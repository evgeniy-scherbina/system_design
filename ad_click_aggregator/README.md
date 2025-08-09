# Ad Click Aggregator

A real-time data processing pipeline using Apache Kafka, Apache Flink, and ClickHouse for aggregating ad click events.

## Architecture

```
Kafka (ad-clicks topic) → Flink (stream processing) → ClickHouse (OLAP storage)
```

### Components

- **Apache Kafka**: Message streaming platform for ad click events
- **Apache Flink**: Stream processing engine for real-time aggregation
- **ClickHouse**: Columnar OLAP database for fast analytical queries
- **Kafka UI**: Web interface for monitoring Kafka topics and messages
- **ClickHouse UI**: Web interface for querying ClickHouse data

## Prerequisites

- Docker and Docker Compose
- Java 11 or higher
- Maven 3.6 or higher

## Quick Start

### 1. Start the Infrastructure

Start all services using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- Zookeeper (port 2181)
- Kafka (port 9092)
- Kafka UI (http://localhost:8080)
- Flink JobManager (http://localhost:8081)
- Flink TaskManager
- ClickHouse (port 8123)
- ClickHouse UI (http://localhost:8082)

### 2. Build the Flink Application

```bash
mvn clean package
```

### 3. Submit the Flink Job

```bash
# Copy the JAR to Flink
docker cp target/ad-click-aggregator-1.0-SNAPSHOT.jar flink-jobmanager:/opt/flink/usrlib/

# Submit the job
docker exec flink-jobmanager flink run /opt/flink/usrlib/ad-click-aggregator-1.0-SNAPSHOT.jar
```

### 4. Generate Sample Data

```bash
mvn exec:java -Dexec.mainClass="com.example.DataGenerator"
```

### 5. Monitor the Pipeline

- **Flink Dashboard**: http://localhost:8081
- **Kafka UI**: http://localhost:8080
- **ClickHouse UI**: http://localhost:8082

## Data Flow

### 1. Ad Click Events

The system processes JSON ad click events with the following structure:

```json
{
  "ad_id": "ad_001",
  "user_id": "user_123456",
  "campaign_id": "campaign_001",
  "timestamp": "2024-01-15T10:30:00Z",
  "click_value": 2.50,
  "country": "US",
  "device_type": "mobile"
}
```

### 2. Flink Processing

Flink performs the following operations:
- Reads JSON events from Kafka topic `ad-clicks`
- Parses and validates the events
- Groups by campaign, country, and device type
- Applies 5-minute tumbling windows
- Aggregates click counts, total values, and unique users
- Writes results to ClickHouse

### 3. ClickHouse Storage

Aggregated data is stored in the `click_aggregations` table:

```sql
CREATE TABLE click_aggregations (
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
```

## Querying Data

### Example Queries

1. **Total clicks by campaign in the last hour**:
```sql
SELECT 
    campaign_id,
    sum(click_count) as total_clicks,
    sum(total_value) as total_revenue
FROM click_aggregations 
WHERE window_start >= now() - INTERVAL 1 HOUR
GROUP BY campaign_id
ORDER BY total_clicks DESC;
```

2. **Performance by country and device**:
```sql
SELECT 
    country,
    device_type,
    sum(click_count) as clicks,
    sum(total_value) as revenue,
    avg(total_value / click_count) as avg_cpc
FROM click_aggregations 
WHERE window_start >= now() - INTERVAL 1 HOUR
GROUP BY country, device_type
ORDER BY revenue DESC;
```

3. **Real-time metrics**:
```sql
SELECT 
    campaign_id,
    sum(click_count) as total_clicks,
    sum(unique_users) as unique_users,
    sum(total_value) as total_revenue
FROM click_aggregations 
WHERE window_start >= now() - INTERVAL 30 MINUTE
GROUP BY campaign_id;
```

## Development

### Project Structure

```
├── docker-compose.yml          # Infrastructure setup
├── pom.xml                     # Maven dependencies
├── src/main/java/com/example/
│   ├── AdClickEvent.java       # Data model for click events
│   ├── ClickAggregation.java   # Data model for aggregations
│   ├── AdClickAggregator.java  # Main Flink application
│   ├── ClickHouseSink.java     # Custom sink for ClickHouse
│   └── DataGenerator.java      # Sample data generator
└── clickhouse/                 # ClickHouse configuration
    ├── config/
    └── users/
```

### Key Features

- **Real-time Processing**: 5-minute tumbling windows for near real-time aggregation
- **Fault Tolerance**: Flink provides exactly-once processing semantics
- **Scalability**: Horizontal scaling with multiple Flink TaskManagers
- **High Performance**: ClickHouse provides sub-second query performance
- **Monitoring**: Web UIs for both Kafka and ClickHouse

### Configuration

#### Kafka Topics
- `ad-clicks`: Input topic for ad click events

#### Flink Settings
- Checkpoint interval: 30 seconds
- Watermark strategy: 5-second out-of-order tolerance
- Window size: 5 minutes

#### ClickHouse Settings
- Memory limit: 1GB
- MergeTree engine for optimal analytical performance
- Ordered by (campaign_id, country, device_type, window_start)

## Troubleshooting

### Common Issues

1. **Flink job fails to start**:
   - Check if Kafka and ClickHouse are running
   - Verify network connectivity between containers
   - Check Flink logs: `docker logs flink-jobmanager`

2. **No data in ClickHouse**:
   - Verify the Flink job is running
   - Check if data is being produced to Kafka
   - Monitor ClickHouse logs: `docker logs clickhouse`

3. **High memory usage**:
   - Adjust ClickHouse memory settings in `clickhouse/config/01-custom.xml`
   - Reduce Flink parallelism if needed

### Useful Commands

```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs kafka
docker-compose logs flink-jobmanager
docker-compose logs clickhouse

# Restart services
docker-compose restart kafka
docker-compose restart flink-jobmanager

# Clean up
docker-compose down -v
```

## Performance Tuning

### For Production Use

1. **Kafka**:
   - Increase partitions for better parallelism
   - Configure retention policies
   - Set appropriate replication factor

2. **Flink**:
   - Increase parallelism based on CPU cores
   - Configure checkpoint storage (S3, HDFS)
   - Set appropriate state backend

3. **ClickHouse**:
   - Increase memory limits
   - Configure proper storage policies
   - Set up replication for high availability

## License

This project is for educational purposes. Feel free to use and modify as needed. 