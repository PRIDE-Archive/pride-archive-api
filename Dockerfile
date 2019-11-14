FROM maven:3.3.9-jdk-8-alpine

# Create app directory
WORKDIR /pride-api

RUN mvn clean package
COPY target/*.jar ./

CMD [ "java", "-jar", "*.jar" ]
