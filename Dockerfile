# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline || true

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd -r -u 10001 appuser
COPY --from=build /workspace/target/*.jar /app/app.jar

ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080
USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
