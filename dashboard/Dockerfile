FROM adoptopenjdk:16-jdk-hotspot AS builder

WORKDIR /dunctebot-dashboard
COPY gradle ./gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN ./gradlew --no-daemon dependencies
COPY . .
RUN ./gradlew --no-daemon build

FROM adoptopenjdk:16-jre-hotspot

WORKDIR /dunctebot-dashboard
COPY --from=builder /dunctebot-dashboard/build/libs/dunctebot-dashboard*.jar ./dashboard.jar

ENTRYPOINT ["java", "-jar", "dashboard.jar"]
