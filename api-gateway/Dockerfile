# Stage 1: Build the JAR using Maven
FROM maven:3.8.5-eclipse-temurin-17 as build

WORKDIR /app

# Copy only pom.xml and download dependencies (for better caching)
COPY pom.xml .

RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the project (skip tests for faster build; remove -DskipTests if you want tests)
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
