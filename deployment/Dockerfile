FROM gradle:8.14.3-jdk21-alpine AS builder

WORKDIR /ms-applications

COPY build.gradle settings.gradle main.gradle gradlew gradlew.bat gradle.properties ./
COPY gradle/ ./gradle/

COPY applications/ ./applications/
COPY domain/ ./domain/
COPY infrastructure/ ./infrastructure/

RUN gradle dependencies
RUN gradle :app-service:bootJar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /ms-applications/applications/app-service/build/libs/*.jar app.jar

RUN chown appuser:appgroup /app/app.jar
USER appuser

EXPOSE 8080

ENV JAVA_OPTS=" -XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar app.jar" ]
