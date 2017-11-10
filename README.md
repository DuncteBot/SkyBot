# SkyBot [![Build Status](https://travis-ci.org/duncte123/SkyBot.svg?branch=travis)](https://travis-ci.org/duncte123/SkyBot)
SkyBot is a discord bot with music and mod commands

# Libs
The following libraries are used:
[_JDA_](https://github.com/DV8FromTheWorld/JDA) 
,[_LavaPlayer_](https://github.com/sedmelluq/lavaplayer)
,[_jda-nas_](https://github.com/sedmelluq/jda-nas), commons-lang3, commons-text, jsoup, mysql-connector-java, gson, groovy-jsr223 and logback-classic

# How to setup?
## Replace the X.X.X with the latest version
Download the latest release here: [Releases](https://github.com/duncte123/SkyBot/releases)
You need the `skybot-X.X.X-all.jar`

Execute the following command
```shell
java -jar skybot-X.X.X-all.jar
```
The bot will create a file named `config.json`.

Edit this file with your settings

Needed entries:

```json
{
  "discord": {
    "token": "<insert your discord token here>",
    "prefix": "/",
    "totalShards": 1
  },
  "use_database": false,
  "sql": {
    "host": "",
    "username": "",
    "password": "",
    "database": ""
  },
  "apis" : {
    "googl" : "",
    "thecatapi": "",
    "wolframalpha": ""
  }
}
```

Go [here](https://discordapp.com/developers/applications/me) and add a new application. After that click on `Create a Bot User` and copy the token and replace it in `config-template.json`.

In the above example you would replace `<insert your discord token here>` with the generated token by Discord.

After that rename `config-template.json` to `config.json`.

Then rerun

```shell
java -jar skybot-X.X.X-all.jar
```

# How to invite the bot to a server?

Go to `https://discordapp.com/oauth2/authorize?&client_id=<CLIENT_ID>&scope=bot&permissions=0` and replace `<CLIENT_ID>` with the client id of the bot on the top of the page. Then you will be asked, on which server the bot should join.

# live demo
You can find a live demo [_here_](https://discord.gg/XBQ9xAT)

# ToDo list
- [X] SQLite as a fail over database
- [X] Make database optional
- [X] Create Joke command
- [X] Fix timed bans in the ban command (Turned out to be a backend issue)
- [ ] Add a wiki that explains how to set the bot up yourself
- [X] Create new way to load the bot token
- [X] Create a random seal command
- [X] Make the nowplaying command show the title of the song in the embed
- [x] Make kpop command take in a search term (name/id)

