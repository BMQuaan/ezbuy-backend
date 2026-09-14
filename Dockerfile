# ==========================================
# Build Stage
# ==========================================
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and POM first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Normalize line endings for Windows environments and fetch dependencies
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ==========================================
# Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy packaged JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot app runs on port 8081
EXPOSE 8081

# Run with docker profile
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]