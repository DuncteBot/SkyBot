FROM azul/zulu-openjdk-alpine:21 AS builder

WORKDIR /skybot
COPY . .
RUN ./gradlew --no-daemon :dashboard:build

FROM azul/zulu-openjdk-alpine:21-jre

WORKDIR /skybot
COPY --from=builder /skybot/dashboard/build/libs/dashboard*-all.jar ./dashboard.jar

ENTRYPOINT ["java", "-jar", "dashboard.jar"]
