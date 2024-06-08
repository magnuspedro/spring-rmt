#!/usr/bin/env bash

mvn install -f config-starter/pom.xml
mvn install -f detection-and-refactoring/pom.xml
mvn install -f refactoring-and-metrics-manager/pom.xml
mvn install -f metrics-calculator/pom.xml

cd detection-and-refactoring
docker build -t magnus/detection .

cd  ..

cd refactoring-and-metrics-manager
docker build -t magnus/manager .

cd ..

cd metrics-calculator
docker build -t magnus/metrics .

