FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY . .
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /

COPY --from=build /app/target/auth_service-0.0.1.jar app-auth.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app-auth.jar"]