[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8ed5b37f438d4dbc894b618a5b6f76a6)](https://app.codacy.com/app/duncte123/SkyBot?utm_source=github.com&utm_medium=referral&utm_content=duncte123/SkyBot&utm_campaign=badger)
# SkyBot [![Build Status](https://circleci.com/gh/duncte123/SkyBot/tree/master.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/duncte123/SkyBot) [![DiscordBots](https://discordbots.org/api/widget/status/210363111729790977.png)](https://discordbots.org/bot/210363111729790977)
SkyBot is a discord bot with music and mod commands

# Support
If you need any support please join our [support guild](https://discord.gg/NKM9Xtk) [![Support Guild](https://discordapp.com/api/guilds/191245668617158656/embed.png)](https://discord.gg/NKM9Xtk)


# How to setup?
[Click here](https://github.com/duncte123/SkyBot/wiki/How-to-run-the-bot) to learn how to set the bot up

# How to invite the bot to a server?

Go to `https://discordapp.com/oauth2/authorize?&client_id=<CLIENT_ID>&scope=bot&permissions=0` and replace `<CLIENT_ID>` with the client id of the bot on the top of the page. Then you will be asked, on which server the bot should join.

# Updater
The update command requires an external application to work, to get the updater working please use [this application](https://github.com/ramidzkh/SkyBot-Updater/releases) to run SkyBot: [https://github.com/ramidzkh/SkyBot-Updater/releases](https://github.com/ramidzkh/SkyBot-Updater/releases)

# Libs
The following libraries are used:
- [_weeb.java_](https://github.com/duncte123/weeb.java)
- [_JDA_](https://github.com/DV8FromTheWorld/JDA)
- [_LavaPlayer_](https://github.com/sedmelluq/lavaplayer)
- [_jda-nas_](https://github.com/sedmelluq/jda-nas)
- [_ason_](https://github.com/afollestad/ason)
- [_reflections_](https://github.com/ronmamo/reflections)
- [_commons-text_](https://commons.apache.org/proper/commons-text/)
- [_jsoup_](https://jsoup.org/)
- [_mysql-connector-java_](https://dev.mysql.com/downloads/connector/j/)
- [_sqlite-connector-java_](https://github.com/xerial/sqlite-jdbc)
- [_groovy-jsr223_](https://github.com/apache/groovy)
- [_groovy-sandbox_](https://github.com/jenkinsci/groovy-sandbox)
- [_spotify-web-api-java_](https://github.com/thelinmichael/spotify-web-api-java)
- [_logback-classic_](https://logback.qos.ch/)
- [_chatter-bot-api_](https://github.com/pierredavidbelanger/chatter-bot-api)

# ToDo list
- [x] ~~SQLite as a fail over database~~
- [x] ~~Make database optional~~
- [x] ~~Create Joke command~~
- [x] ~~Fix timed bans in the ban command (Turned out to be a backend issue)~~
- [x] ~~Add a wiki that explains how to set the bot up yourself~~
- [x] ~~Create new way to load the bot token~~
- [x] ~~Create a random seal command~~
- [x] ~~Make the nowplaying command show the title of the song in the embed~~
- [x] ~~Make kpop command take in a search term (name/id)~~
- [ ] Find international music streams for the [RadioCommand](https://github.com/duncte123/SkyBot/blob/dev/src/main/kotlin/ml/duncte123/skybot/commands/music/RadioCommand.kt#L69)
- [X] ~~Make web requests thread safe~~
