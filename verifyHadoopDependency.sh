#!/usr/bin/env bash

./gradlew :runners:spark:build :runners:spark:job-server:build :sdks:java:extensions:sorter:build :sdks:java:io:hadoop-common:build :sdks:java:io:hadoop-file-system:build :sdks:java:io:hadoop-format:build :sdks:java:io:hbase:build :sdks:java:io:hcatalog:build :sdks:java:io:parquet:build :sdks:java:maven-archetypes:examples:build
