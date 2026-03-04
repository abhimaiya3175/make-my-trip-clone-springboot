# Use an official Maven image with Java 21 for building
FROM maven:3.9.5-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the project files
COPY . .

# Build the project without running tests
RUN mvn clean package -DskipTests

# Use a lightweight JDK 21 image for running the application
FROM eclipse-temurin:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy only the built JAR file from the previous stage
COPY --from=build /app/target/makemytrip-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
