package com.example;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.util.Collector;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WordCount {

    public static void main(String[] args) throws Exception {
        // Set up the batch execution environment
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // Read input text file
        DataSet<String> text = env.readTextFile("input.txt");

        DataSet<Tuple2<String, Integer>> totalWordCount = text
            .map(new ClickCounter())
            .groupBy(0)
            .sum(1);
        totalWordCount.print();
    }

    // FlatMap function to split lines into (word, 1) tuples for total word count
    public static class WordTokenizer implements org.apache.flink.api.common.functions.FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(String line, Collector<Tuple2<String, Integer>> out) {
            for (String word : line.toLowerCase().split("\\W+")) {
                if (word.length() > 0) {
                    out.collect(new Tuple2<>(word, 1));
                }
            }
        }
    }

    public static class ClickCounter implements org.apache.flink.api.common.functions.MapFunction<String, Tuple2<String, Integer>> {
        @Override
        public Tuple2<String, Integer> map(String jsonString) throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            ClickEvent event = mapper.readValue(jsonString, ClickEvent.class);
            return new Tuple2<>(event.userId, 1);
        }
    }
    
    public static class ClickEvent {
        public String userId;
        public String timestamp;
        public String url;
    }
} 