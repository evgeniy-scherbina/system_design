package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DataGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random random = new Random();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private static final String[] CAMPAIGNS = {"campaign_001", "campaign_002", "campaign_003", "campaign_004"};
    private static final String[] COUNTRIES = {"US", "UK", "CA", "DE", "FR", "JP", "AU", "BR"};
    private static final String[] DEVICE_TYPES = {"mobile", "desktop", "tablet"};
    private static final String[] AD_IDS = {"ad_001", "ad_002", "ad_003", "ad_004", "ad_005"};

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            LOG.info("Starting data generation...");
            
            int messageCount = 0;
            while (true) {
                AdClickEvent event = generateRandomEvent();
                String json = objectMapper.writeValueAsString(event);
                
                ProducerRecord<String, String> record = 
                    new ProducerRecord<>("ad-clicks", event.getCampaignId(), json);
                
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        LOG.error("Error sending message", exception);
                    } else {
                        LOG.debug("Message sent to topic {} partition {} offset {}", 
                                 metadata.topic(), metadata.partition(), metadata.offset());
                    }
                });
                
                messageCount++;
                if (messageCount % 100 == 0) {
                    LOG.info("Generated {} messages", messageCount);
                }
                
                // Generate 1-5 events per second
                Thread.sleep(random.nextInt(1000) + 200);
            }
        }
    }

    private static AdClickEvent generateRandomEvent() {
        String campaignId = CAMPAIGNS[random.nextInt(CAMPAIGNS.length)];
        String adId = AD_IDS[random.nextInt(AD_IDS.length)];
        String userId = "user_" + String.format("%06d", random.nextInt(100000));
        String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
        String deviceType = DEVICE_TYPES[random.nextInt(DEVICE_TYPES.length)];
        
        // Generate timestamp within the last hour
        Instant timestamp = Instant.now().minusSeconds(random.nextInt(3600));
        
        // Generate click value between 0.01 and 10.00
        Double clickValue = 0.01 + random.nextDouble() * 9.99;
        
        return new AdClickEvent(adId, userId, campaignId, timestamp, clickValue, country, deviceType);
    }
} 