# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build-env

# Create app directory
WORKDIR /app

COPY src ./src
COPY pom.xml ./
RUN mvn clean package -DjarFinalName=${JAR_FILE_NAME}

# Package stage
FROM maven:3.9.8-amazoncorretto-21

WORKDIR /app
COPY --from=build-env /app/target/${JAR_FILE_NAME}.jar ./
#COPY ${APM_AGENT_JAR} ./

#ENTRYPOINT java ${APM_AGENT_OPTS} ${JAVA_OPTS} -jar ${JAR_FILE_NAME}.jar
ENTRYPOINT java ${JAVA_OPTS} -jar ${JAR_FILE_NAME}.jar
