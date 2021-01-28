FROM adoptopenjdk:15-jdk-hotspot AS builder

WORKDIR /skybot
COPY gradle ./gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN ./gradlew --no-daemon dependencies
COPY . .
RUN ./gradlew --no-daemon build

FROM adoptopenjdk:15-jre-hotspot

WORKDIR /skybot
COPY --from=builder /skybot/build/libs/skybot*.jar ./skybot.jar

CMD ["java", "-jar", "skybot.jar"]
