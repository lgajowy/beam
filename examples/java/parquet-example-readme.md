# Proof of Concept: running ParquetIO with Spark and Flink

Below are running instructions. *Note that Gradle is not yet supported* 

## Spark


### Building:
```
mvn clean package -Pspark-runner -DskipTests
```

### Writing Parquet files
```
spark-submit --class org.apache.beam.examples.parquet.ParquetWrite --master spark://LGs-Mac.local:7077 target/beam-examples-java-2.5.0-SNAPSHOT-shaded.jar --runner=SparkRunner --filenamePrefix="/Users/lukasz/parquet-demo/beam/" --numberOfRecords=1000 
```


### Reading Parquet files:
```
spark-submit --class org.apache.beam.examples.parquet.ParquetRead --master spark://LGs-Mac.local:7077 target/beam-examples-java-2.5.0-SNAPSHOT-shaded.jar --runner=SparkRunner --filenamePrefix="/Users/lukasz/parquet-demo/beam/*"
```

# Flink

### Building:
```
mvn clean package -Pflink-runner -DskipTests
```

### Writing Parquet files
```
mvn exec:java -Dexec.mainClass=org.apache.beam.examples.parquet.ParquetWrite -Pflink-runner -Dexec.args="--runner=FlinkRunner --flinkMaster=localhost:6123 --filesToStage=target/beam-examples-java-2.5.0-SNAPSHOT-shaded.jar" --filenamePrefix="/Users/lukasz/parquet-demo/beam/" --numberOfRecords=1000 

```

### Reading Parquet files:
```
mvn exec:java -Dexec.mainClass=org.apache.beam.examples.parquet.ParquetRead -Pflink-runner -Dexec.args="--runner=FlinkRunner --flinkMaster=localhost:6123 --filesToStage=target/beam-examples-java-2.5.0-SNAPSHOT-shaded.jar" --filenamePrefix="/Users/lukasz/parquet-demo/beam/*"
```

# Direct

### Building:
```
mvn clean package -Pdirect-runner -DskipTests
```

### Writing Parquet files:

```
mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.parquet.ParquetWrite -Pdirect-runner --filesToStage=target/beam-examples-java-2.5.0-SNAPSHOT-shaded.jar" --filenamePrefix="/Users/lukasz/parquet-demo/beam"
```
