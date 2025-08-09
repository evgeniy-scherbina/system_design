# Flink Word Count Example

A simple Apache Flink batch processing example that counts word occurrences in a text file.

## Prerequisites

- Java 8 or higher
- Maven 3.6 or higher
- Apache Flink (will be downloaded automatically via Maven)

## Project Structure

```
flink_word_count/
├── pom.xml                    # Maven configuration
├── input.txt                  # Sample input file
├── src/main/java/com/example/
│   └── WordCount.java         # Main Flink application
└── README.md                  # This file
```

## Building the Project

```bash
mvn clean package
```

This will create a JAR file in the `target/` directory.

## Running the Application

### Option 1: Run with Maven (for development)

```bash
mvn exec:java -Dexec.mainClass="com.example.WordCount"
```

### Option 2: Run with Flink CLI

First, start a local Flink cluster:

```bash
# Download Flink if you haven't already
wget https://archive.apache.org/dist/flink/flink-1.18.1/flink-1.18.1-bin-scala_2.12.tgz
tar -xzf flink-1.18.1-bin-scala_2.12.tgz
cd flink-1.18.1

# Start local cluster
./bin/start-cluster.sh
```

Then submit the job:

```bash
./bin/flink run /path/to/your/flink-word-count/target/flink-word-count-1.0-SNAPSHOT.jar
```

### Option 3: Run in IDE

You can also run the `WordCount` class directly in your IDE. Make sure the `input.txt` file is in the project root directory.

## Expected Output

The application will output word counts similar to:

```
(a,1)
(again,1)
(apache,1)
(basic,1)
(brown,1)
(count,1)
(counting,1)
(demonstrates,1)
(distributed,1)
(dog,1)
(example,2)
(framework,1)
(fox,1)
(functionality,1)
(hello,2)
(is,2)
(jumps,1)
(lazy,1)
(over,1)
(processing,1)
(quick,1)
(simple,1)
(stream,1)
(the,2)
(this,2)
(world,2)
```

## How It Works

1. **Input**: Reads text from `input.txt`
2. **Tokenization**: Splits each line into words using regex `\\W+` (non-word characters)
3. **Mapping**: Creates tuples of `(word, 1)` for each word
4. **Grouping**: Groups by word
5. **Summing**: Sums the counts for each word
6. **Output**: Prints the final word counts

## Customization

- Change the input file path in `WordCount.java` line 18
- Modify the tokenization logic in the `Tokenizer` class
- Add filtering or additional transformations as needed 