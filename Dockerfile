# Build stage
FROM maven:3.3.9-jdk-8-alpine AS build-env

# Create app directory
WORKDIR /pride-api

COPY src ./src
COPY pom.xml ./
COPY config/application.yml ./application.yml
RUN mvn clean package

# Package stage
FROM maven:3.3.9-jdk-8-alpine
WORKDIR /pride-api
COPY --from=build-env /pride-api/target/pride-api.jar ./
CMD ["java", "-jar", "pride-api.jar"]