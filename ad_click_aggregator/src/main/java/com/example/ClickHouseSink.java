package com.example;

import com.clickhouse.jdbc.ClickHouseDataSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ClickHouseSink extends RichSinkFunction<ClickAggregation> {
    private static final Logger LOG = LoggerFactory.getLogger(ClickHouseSink.class);
    
    private ClickHouseDataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    
    private static final String INSERT_SQL = 
        "INSERT INTO click_aggregations (campaign_id, country, device_type, window_start, window_end, click_count, total_value, unique_users) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void open(Configuration parameters) throws Exception {
        // Initialize ClickHouse connection
        String url = "jdbc:clickhouse://localhost:8123/default";
        dataSource = new ClickHouseDataSource(url);
        connection = dataSource.getConnection();
        preparedStatement = connection.prepareStatement(INSERT_SQL);
        
        // Create table if not exists
        createTableIfNotExists();
        
        LOG.info("ClickHouse sink initialized successfully");
    }

    private void createTableIfNotExists() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS click_aggregations (
                campaign_id String,
                country String,
                device_type String,
                window_start DateTime,
                window_end DateTime,
                click_count UInt64,
                total_value Float64,
                unique_users UInt64
            ) ENGINE = MergeTree()
            ORDER BY (campaign_id, country, device_type, window_start)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
            stmt.execute();
            LOG.info("ClickHouse table created or already exists");
        }
    }

    @Override
    public void invoke(ClickAggregation aggregation, Context context) throws Exception {
        try {
            preparedStatement.setString(1, aggregation.getCampaignId());
            preparedStatement.setString(2, aggregation.getCountry());
            preparedStatement.setString(3, aggregation.getDeviceType());
            preparedStatement.setString(4, aggregation.getWindowStart().toString());
            preparedStatement.setString(5, aggregation.getWindowEnd().toString());
            preparedStatement.setLong(6, aggregation.getClickCount());
            preparedStatement.setDouble(7, aggregation.getTotalValue());
            preparedStatement.setLong(8, aggregation.getUniqueUsers());
            
            preparedStatement.executeUpdate();
            
            LOG.debug("Inserted aggregation: {}", aggregation);
        } catch (SQLException e) {
            LOG.error("Error inserting aggregation into ClickHouse: {}", aggregation, e);
            throw e;
        }
    }

    @Override
    public void close() throws Exception {
        if (preparedStatement != null) {
            preparedStatement.close();
        }
        if (connection != null) {
            connection.close();
        }
        LOG.info("ClickHouse sink closed");
    }
} 