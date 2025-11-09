# Build
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test --no-daemon

# Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
CMD ["sh","-c","java -Djava.net.preferIPv4Stack=false -jar app.jar --server.address=:: --server.port=${PORT}"]
