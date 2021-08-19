FROM azul/zulu-openjdk-alpine:16 AS builder

WORKDIR /skybot

# setup CI flag
ARG is_ci
ARG git_sha
ENV CI=$is_ci
ENV GIT_HASH=$git_sha

COPY gradle ./gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN ./gradlew --no-daemon dependencies
COPY . .
RUN ./gradlew --no-daemon build

FROM azul/zulu-openjdk-alpine:16-jre

WORKDIR /skybot
COPY --from=builder /skybot/build/libs/skybot*.jar ./skybot.jar

CMD ["java", "-jar", "skybot.jar"]
