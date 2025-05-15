# Use official Maven image with Java 17
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Use official Eclipse Temurin JDK runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/restaurant-auth-*.jar restaurant-auth.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "restaurant-auth.jar"]