FROM gradle:8.14.3-jdk21-alpine AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew

RUN gradle dependencies --no-daemon

COPY src src
RUN gradle build --no-daemon -x test

RUN java -Djarmode=layertools -jar build/libs/*.jar extract

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

COPY --from=builder --chown=appuser:appuser /app/dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appuser /app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/application/ ./

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]