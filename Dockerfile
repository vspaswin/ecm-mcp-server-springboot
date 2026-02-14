# Multi-stage build for ECM MCP Server

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1000 ecmserver && \
    adduser -u 1000 -G ecmserver -s /bin/sh -D ecmserver

# Copy JAR from build stage
COPY --from=build /app/target/ecm-mcp-server-*.jar /app/ecm-mcp-server.jar

# Change ownership
RUN chown -R ecmserver:ecmserver /app

# Switch to non-root user
USER ecmserver

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "ecm-mcp-server.jar"]
