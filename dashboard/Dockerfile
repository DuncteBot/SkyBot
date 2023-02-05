FROM azul/zulu-openjdk-alpine:19 AS builder

WORKDIR /skybot
COPY . .
RUN ./gradlew --no-daemon :dashboard:build

FROM azul/zulu-openjdk-alpine:19-jre

WORKDIR /skybot
COPY --from=builder /skybot/dashboard/build/libs/dashboard*.jar ./dashboard.jar

ENTRYPOINT ["java", "-jar", "dashboard.jar"]
