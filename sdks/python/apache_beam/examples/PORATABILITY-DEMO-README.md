# Portability demo

1. Build the SDK Container:

```
./gradlew -p sdks/python/container docker
```


1. Start Job Server:

```
./gradlew :beam-runners-flink_2.11-job-server:runShadow -PflinkMasterUrl=localhost:8081
```

1. Start wordcount.py example:

```
./gradlew :beam-sdks-python:portableWordCount -PjobEndpoint=localhost:8099
```
