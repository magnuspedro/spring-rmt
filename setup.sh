#!/usr/bin/env bash

mvn install -f detection-agent/pom.xml
mvn install -f intermediary-agent/pom.xml
mvn install -f metrics-agent/pom.xml

cd detection-agent
docker build -t magnus/detection .

cd  ..

cd intermediary-agent
docker build -t magnus/intermediary .

cd ..

cd metrics-agent
docker build -t magnus/metrics .

