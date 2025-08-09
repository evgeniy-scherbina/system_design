package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class AdClickAggregator {
    private static final Logger LOG = LoggerFactory.getLogger(AdClickAggregator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static void main(String[] args) throws Exception {
        // Set up the streaming execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Configure Kafka source
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("ad-clicks")
                .setGroupId("ad-click-aggregator")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Create the data stream
        DataStream<AdClickEvent> clickStream = env
                .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source")
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, AdClickEvent.class);
                    } catch (Exception e) {
                        LOG.error("Error parsing JSON: {}", json, e);
                        return null;
                    }
                })
                .filter(event -> event != null)
                .assignTimestampsAndWatermarks(
                    WatermarkStrategy
                        .<AdClickEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                        .withTimestampAssigner((event, timestamp) -> event.getTimestamp().toEpochMilli())
                );

        // Apply windowing and aggregation
        DataStream<ClickAggregation> aggregatedStream = clickStream
                .keyBy(event -> new ClickKey(event.getCampaignId(), event.getCountry(), event.getDeviceType()))
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                .aggregate(new ClickAggregator());

        // Write to ClickHouse
        aggregatedStream.addSink(new ClickHouseSink());

        // Execute the job
        LOG.info("Starting Ad Click Aggregator job...");
        env.execute("Ad Click Aggregator");
    }

    // Key class for grouping
    public static class ClickKey {
        private final String campaignId;
        private final String country;
        private final String deviceType;

        public ClickKey(String campaignId, String country, String deviceType) {
            this.campaignId = campaignId;
            this.country = country;
            this.deviceType = deviceType;
        }

        public String getCampaignId() { return campaignId; }
        public String getCountry() { return country; }
        public String getDeviceType() { return deviceType; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClickKey clickKey = (ClickKey) o;
            return campaignId.equals(clickKey.campaignId) &&
                   country.equals(clickKey.country) &&
                   deviceType.equals(clickKey.deviceType);
        }

        @Override
        public int hashCode() {
            int result = campaignId.hashCode();
            result = 31 * result + country.hashCode();
            result = 31 * result + deviceType.hashCode();
            return result;
        }
    }

    // Custom aggregate function
    public static class ClickAggregator implements AggregateFunction<AdClickEvent, ClickAggregator.Accumulator, ClickAggregation> {
        
        public static class Accumulator {
            public String campaignId;
            public String country;
            public String deviceType;
            public long clickCount = 0;
            public double totalValue = 0.0;
            public Set<String> uniqueUsers = new HashSet<>();
            public long minTimestamp = Long.MAX_VALUE;
            public long maxTimestamp = Long.MIN_VALUE;
        }

        @Override
        public Accumulator createAccumulator() {
            return new Accumulator();
        }

        @Override
        public Accumulator add(AdClickEvent event, Accumulator acc) {
            if (acc.campaignId == null) {
                acc.campaignId = event.getCampaignId();
                acc.country = event.getCountry();
                acc.deviceType = event.getDeviceType();
            }
            
            acc.clickCount++;
            acc.totalValue += (event.getClickValue() != null ? event.getClickValue() : 0.0);
            acc.uniqueUsers.add(event.getUserId());
            
            long timestamp = event.getTimestamp().toEpochMilli();
            acc.minTimestamp = Math.min(acc.minTimestamp, timestamp);
            acc.maxTimestamp = Math.max(acc.maxTimestamp, timestamp);
            
            return acc;
        }

        @Override
        public ClickAggregation getResult(Accumulator acc) {
            return new ClickAggregation(
                acc.campaignId,
                acc.country,
                acc.deviceType,
                Instant.ofEpochMilli(acc.minTimestamp),
                Instant.ofEpochMilli(acc.maxTimestamp),
                acc.clickCount,
                acc.totalValue,
                (long) acc.uniqueUsers.size()
            );
        }

        @Override
        public Accumulator merge(Accumulator a, Accumulator b) {
            a.clickCount += b.clickCount;
            a.totalValue += b.totalValue;
            a.uniqueUsers.addAll(b.uniqueUsers);
            a.minTimestamp = Math.min(a.minTimestamp, b.minTimestamp);
            a.maxTimestamp = Math.max(a.maxTimestamp, b.maxTimestamp);
            return a;
        }
    }
} 