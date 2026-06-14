# ==========================================
# 1. Staging build
# ==========================================
# Build the application
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy the project source code into the container.
COPY . .

# Disable daemon to save memory and build excluding tests.
RUN ./gradlew build -x test --no-daemon

# ==========================================
# 2. Staging run
# ==========================================
# Setup a lightweight Java runtime environment for execution.
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy only the build artifact (jar) from the build stage.
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# Run with a 300MB memory limit (512MB).
ENTRYPOINT ["java", "-Xmx300m", "-Xms300m", "-jar", "app.jar"]