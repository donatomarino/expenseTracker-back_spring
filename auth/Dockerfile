FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/auth_service-0.0.1.jar
COPY ${JAR_FILE} app-auth.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app-auth.jar"]