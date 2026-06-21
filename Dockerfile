# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (leverages Docker layer caching)
COPY pom.xml .
RUN mvn dependency:resolve

# Copy source code and build
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw .
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/car-rental-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Render will use PORT env variable, but we specify default)
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
