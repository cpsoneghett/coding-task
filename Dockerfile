FROM maven:3.9.9-eclipse-temurin-21 AS builder
COPY . /app
WORKDIR /app
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21 AS prod
COPY --from=builder /app/target/*.jar /app/task.jar
ENV SERVER_PORT=8080
WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/task.jar"]