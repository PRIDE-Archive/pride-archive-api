# Build stage
FROM maven:3.3.9-jdk-8-alpine AS build-env

# Create app directory
WORKDIR /pride-api

COPY src ./src
COPY pom.xml ./
RUN mvn clean package -Djar.finalName=pride-api

# Package stage
FROM maven:3.3.9-jdk-8-alpine
WORKDIR /pride-api
COPY --from=build-env /pride-api/target/pride-api.jar ./
CMD ["java", "-jar", "pride-api.jar"]