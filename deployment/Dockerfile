# -------- Stage 1: Build --------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy full source including all modules and wrapper
COPY . .

# Build only the application jar (not tests, to speed it up)
RUN ./mvnw clean package -DskipTests

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:25-jre
WORKDIR /opt/app

# Copy the built fat jar from the build stage
COPY --from=build /app/crypto-recommender-app/target/*.jar app.jar

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
