# Use an official OpenJDK runtime as a parent image
FROM amazoncorretto:21.0.5 AS builder

# Set the working directory
WORKDIR /app

# Copy the build files
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

# Copy the source code
COPY src ./src

# Build the application
RUN ./gradlew build

# Use a smaller base image for the final stage
FROM amazoncorretto:21.0.5-alpine3.20 AS runner

# Set the working directory
WORKDIR /app

# Copy the built application from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]