FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY . .
RUN ./mvnw package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /

COPY --from=build /app/target/expense_service-0.0.1.jar app-auth.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app-auth.jar"]