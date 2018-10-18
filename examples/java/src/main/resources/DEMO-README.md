#HOWTO:

1. Run the clusters

1. Run on flink like this:
```
./gradlew run -p examples/java/ -PmainClass=org.apache.beam.examples.WordCount --args="--output=/Users/lukasz/Projects/apache-beam/wordcount/demo-1/wc-flink --inputFile=/Users/lukasz/Projects/apache-beam/wordcount/kinglear.txt --runner=FlinkRunner --flinkMaster=localhost:8081 --tempLocation=/tmp/"
```

1. Run on Spark like this:      
```
./gradlew run -p examples/java/ -PmainClass=org.apache.beam.examples.WordCount --args="--output=/Users/lukasz/Projects/apache-beam/wordcount/demo-1/wc-spark --inputFile=/Users/lukasz/Projects/apache-beam/wordcount/kinglear.txt --runner=SparkRunner --sparkMaster=spark://LGs-Mac.local:7077 --tempLocation=/tmp/"
```
