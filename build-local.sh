#!/usr/bin/env bash

mvn clean package -DskipTests=true

docker build --no-cache --platform -t snellocms/snello-api-quarkus -f src/main/docker/Dockerfile.jvm .
ID="$(docker images | grep 'snellocms/snello-api-quarkus' | head -n 1 | awk '{print $3}')"

docker tag snellocms/snello-api-quarkus snellocms/snello-api-quarkus:3.0.0
docker tag snellocms/snello-api-quarkus snellocms/snello-api-quarkus:latest
docker push snellocms/snello-api-quarkus:latest
docker push snellocms/snello-api-quarkus:3.0.0
