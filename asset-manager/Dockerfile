# Stage 1: Build Phase
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy essentially just the POM initially to resolve dependencies efficiently
COPY pom.xml .
COPY src ./src

# Compile the monolithic source directly into a shippable Jar ignoring local tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime Phase
# Use an extremely lightweight alpine java runtime architecture
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Strip the large maven logic and extract only the compiled target binary
COPY --from=build /app/target/asset-manager-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Launch Sequence
ENTRYPOINT ["java", "-jar", "app.jar"]
