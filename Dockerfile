# Build stage
FROM maven:3.8.4-jdk-11-slim AS build-stage

COPY ./backend /usr/src/timechamp/backend
COPY ./frontend /usr/src/timechamp/frontend
COPY ./pom.xml /usr/src/timechamp/pom.xml
WORKDIR /usr/src/timechamp
RUN mvn clean package

# Prod stage
FROM openjdk:11-jre-slim

LABEL maintainer="hi@bluemedia.dev"

ENV DEBIAN_FRONTEND noninteractive

RUN apt update -y && \
    apt upgrade -y && \
    apt clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build-stage /usr/src/timechamp/backend/target/*dependencies.jar /opt/timechamp/timechamp.jar
WORKDIR /opt/timechamp

RUN useradd --system --shell /usr/sbin/nologin timechamp
RUN chown -R timechamp:timechamp /opt/timechamp

USER timechamp
CMD ["java", "-jar", "/opt/timechamp/timechamp.jar"]