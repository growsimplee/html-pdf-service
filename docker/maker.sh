#!/bin/bash
cd ..
mvn clean install -P jar
cd docker
docker-compose build
docker-compose push
