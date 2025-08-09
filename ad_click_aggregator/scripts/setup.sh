#!/bin/bash

set -e

echo "🚀 Setting up Ad Click Aggregator..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven and try again."
    exit 1
fi

echo "📦 Building Flink application..."
mvn clean package

echo "🐳 Starting infrastructure services..."
docker-compose up -d

echo "⏳ Waiting for services to be ready..."
sleep 30

echo "📊 Checking service status..."
docker-compose ps

echo "📋 Submitting Flink job..."
# Wait a bit more for Flink to be fully ready
sleep 10

# Copy JAR to Flink container
docker cp target/ad-click-aggregator-1.0-SNAPSHOT.jar flink-jobmanager:/opt/flink/usrlib/

# Submit the job
docker exec flink-jobmanager flink run /opt/flink/usrlib/ad-click-aggregator-1.0-SNAPSHOT.jar &

echo "✅ Setup complete!"
echo ""
echo "🌐 Access points:"
echo "   - Flink Dashboard: http://localhost:8081"
echo "   - Kafka UI: http://localhost:8080"
echo "   - ClickHouse UI: http://localhost:8082"
echo ""
echo "📈 To generate sample data, run:"
echo "   mvn exec:java -Dexec.mainClass=\"com.example.DataGenerator\""
echo ""
echo "🔍 To view logs:"
echo "   docker-compose logs -f" 