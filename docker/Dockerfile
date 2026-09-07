#FROM openjdk:8-jdk-alpine
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]


#FROM openjdk:8-jdk-alpine
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} app.jar
#ARG OTEL_VERSION=2.9.0
#
## Download OTel Java Agent
#RUN apk --no-cache add wget ca-certificates && \
#    wget "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar" -O /opentelemetry-javaagent.jar
#
#ENTRYPOINT ["java", "-javaagent:/opentelemetry-javaagent.jar","-jar","/app.jar"]

FROM eclipse-temurin:8-jre-alpine

ARG OTEL_VERSION=2.9.0

# Download OTel Java Agent
RUN apk --no-cache add wget ca-certificates && \
    wget "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar" \
      -O /opentelemetry-javaagent.jar && \
    apk del wget

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-javaagent:/opentelemetry-javaagent.jar", "-jar", "/app.jar"]