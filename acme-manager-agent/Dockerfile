# Use the official Maven image to build the application
FROM maven:3.9.8-amazoncorretto-21-al2023 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the root pom.xml file and the acme-manager-agent/pom.xml file
COPY pom.xml .
COPY acme-manager-agent/pom.xml acme-manager-agent/pom.xml

# Copy the source code of the entire project
COPY . .

# Run mvn install to build the entire project
RUN mvn -ntp -B install -DskipTests

# Run mvn package to package the acme-manager-agent module
RUN mvn -ntp -B package -DskipTests -pl acme-manager-agent -am

# Use the official Amazon Corretto image to run the application
FROM amazoncorretto:21-al2023-headless

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/acme-manager-agent/target/acme-manager-agent.jar .

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "acme-manager-agent.jar"]
