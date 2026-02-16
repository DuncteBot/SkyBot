FROM azul/zulu-openjdk-alpine:25 AS builder

WORKDIR /skybot

# setup CI flag
ARG is_ci
ARG git_sha
ENV CI=$is_ci
ENV GIT_HASH=$git_sha

COPY . .
RUN ./gradlew --no-daemon :bot:build

FROM azul/zulu-openjdk-alpine:25-jre

# add libstdc++ for playing back mp3's with lavaplayer
# also add some fonts
RUN apk add --no-cache libstdc++ fontconfig font-noto

COPY ./assets/lottieconverter_musl /skybot/assets/lottieconverter

WORKDIR /skybot
COPY --from=builder /skybot/bot/build/libs/bot*-prod.jar ./skybot.jar

CMD ["java", "-Xms4G", "-Xmx4G", "-jar", "skybot.jar"]
