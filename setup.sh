#!/usr/bin/env bash

mvn install -f config-starter/pom.xml
mvn install -f detectionandrefactoring/pom.xml
mvn install -f refactoringandmetricsmanager/pom.xml
mvn install -f metricscalculator/pom.xml

cd detectionandrefactoring
docker build -t magnus/detection .

cd  ..

cd refactoringandmetricsmanager
docker build -t magnus/manager .

cd ..

cd metricscalculator
docker build -t magnus/metrics .

