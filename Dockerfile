FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/bigprojects-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV MAIL_USERNAME=${MAIL_USERNAME}
ENV MAIL_PASSWORD=${MAIL_PASSWORD}
ENV GEMINI_API_KEY=${GEMINI_API_KEY}
ENV YOUTUBE_API_KEY=${YOUTUBE_API_KEY}
ENTRYPOINT ["java","-jar","app.jar"] 