# Multi-stage build for optimized image size
FROM gradle:8.5-jdk17 AS build
WORKDIR /workspace

# Copy gradle files
COPY settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# Copy source code
COPY services/user-service/build.gradle.kts ./services/user-service/
COPY services/user-service/src ./services/user-service/src

# Build the application
RUN gradle :services:user-service:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the jar from build stage
COPY --from=build /workspace/services/user-service/build/libs/user-service.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
