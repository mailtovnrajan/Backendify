# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the built jar file from gradle to the container
COPY build/libs/backendify-proxy-service.jar /app/backendify.jar

# Expose the correct port (9000)
EXPOSE 9000

# Run the jar file, instructing Spring Boot to listen on port 9000
ENTRYPOINT ["java", "-jar", "backendify.jar"]
