#!/usr/bin/env bash

mvn install -f config-starter/pom.xml
mvn install -f detection-and-refactoring/pom.xml
mvn install -f project-sync-bff/pom.xml
mvn install -f metrics-calculator/pom.xml

cd detection-and-refactoring
docker build -t magnus/detection .

cd  ..

cd project-sync-bff
docker build -t magnus/manager .

cd ..

cd metrics-calculator
docker build -t magnus/metrics .

cd ..

./run_local_full.sh
