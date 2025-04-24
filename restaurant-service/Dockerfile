# Build stage
FROM maven:3.8.6-eclipse-temurin-17 as builder

WORKDIR /workspace/app

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .

# Download dependencies (this step will be cached unless pom.xml changes)
RUN mvn dependency:go-offline

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Extract the built jar
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

VOLUME /tmp

ARG DEPENDENCY=/workspace/app/target/dependency

# Copy the dependencies
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/heap-dump.hprof"

# Create a non-root user and switch to it
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Set the entry point
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -cp app:app/lib/* com.example.restaurantservice.RestaurantServiceApplication"]