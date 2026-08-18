# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build (exclude tests for production build)
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -B

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install wget for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends \
        wget \
    && rm -rf /var/lib/apt/lists/* && \
    # Create non-root user
    addgroup --system lpms && \
    adduser --system --ingroup lpms lpms

# Create directories
RUN mkdir -p /data/documents /data/uploads /tmp /app/logs

# Copy built JAR
COPY --from=build --chown=lpms:lpms /app/target/*.jar app.jar

# Switch to non-root user
USER lpms

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Dfile.encoding=UTF-8"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
