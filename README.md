# SkyBot [![Build Status](https://circleci.com/gh/duncte123/SkyBot/tree/master.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/duncte123/SkyBot) [![DiscordBots](https://discordbots.org/api/widget/status/210363111729790977.png)](https://discordbots.org/bot/210363111729790977)
SkyBot is a discord bot with music and mod commands

# Libs
The following libraries are used:
[_JDA_](https://github.com/DV8FromTheWorld/JDA), 
[_LavaPlayer_](https://github.com/sedmelluq/lavaplayer),
[_jda-nas_](https://github.com/sedmelluq/jda-nas), commons-lang3, commons-text, jsoup, mysql-connector-java, sqlite-connector-java, groovy-all, logback-classic and
[_chatter-bot-api_](https://github.com/pierredavidbelanger/chatter-bot-api)

# How to setup?
[Click here](https://github.com/duncte123/SkyBot/wiki/How-to-run-the-bot) to learn how to set the bot up

# How to invite the bot to a server?

Go to `https://discordapp.com/oauth2/authorize?&client_id=<CLIENT_ID>&scope=bot&permissions=0` and replace `<CLIENT_ID>` with the client id of the bot on the top of the page. Then you will be asked, on which server the bot should join.

# Updater
The update command requires an external application to work, to get the updater working please use [this application](https://github.com/ramidzkh/SkyBot-Updater/releases) to run SkyBot: [https://github.com/ramidzkh/SkyBot-Updater/releases](https://github.com/ramidzkh/SkyBot-Updater/releases)

# Support
If you need any support please join our [support guild](https://discord.gg/NKM9Xtk) [![Support Guild](https://discordapp.com/api/guilds/191245668617158656/embed.png)](https://discord.gg/NKM9Xtk)

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
- [x] Find international music streams for the [RadioCommand](https://github.com/duncte123/SkyBot/blob/dev/src/main/kotlin/ml/duncte123/skybot/commands/music/RadioCommand.kt#L69)
