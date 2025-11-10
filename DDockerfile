FROM maven:3.9.6-eclipse-temurin-21

# Set the working directory inside the container
WORKDIR /app

COPY pom.xml /app/
COPY src /app/src/

# Rebuild project inside the container
RUN mvn clean package -DskipTests

# Expose the port if your Java application listens on a specific port
EXPOSE 4030

ENTRYPOINT ["java", "-jar", "/app/target/RestaurantSystem-0.0.1-SNAPSHOT.jar"]