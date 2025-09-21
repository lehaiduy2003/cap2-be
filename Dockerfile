# Build
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build

# Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
CMD ["sh","-c","java -jar app.jar --server.port=${PORT}"]
