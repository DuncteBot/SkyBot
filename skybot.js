const Discord  = require("discord.js"),
    config = require("./config.json"),
    log = require("fancy-log"),
    commands = require("./utils/commandUtils"),
    bot = new Discord.Client(),
    settings = require("./utils/settingsUtils"),
    ownerId = "191231307290771456";

bot.on('ready', () => {
    //Set default prefixes
    for(let guild of bot.guilds.array()) {
        settings.prefixes[guild.id] = "//";
    }
    bot.user.setPresence({
        status: "online",
        game: {
            name: "SkyBot 4.0",
            type: 1,
            url: "https://www.twitch.tv/duncte123"
        }
    });
    log(`Logged in as ${bot.user.username}#${bot.user.discriminator}`);
});

bot.on("message", message => {
   if(message.channel.type === "text") {
       handleCommand(message);
   }
});

function handleCommand(message) {
    let msg = message.content,
        channel = message.channel,
        author = message.author,
        guild = message.guild,
        prefix = settings.prefixes[guild.id];

    if(msg === prefix+"shutdown" && author.id === ownerId) {
        bot.user.setStatus("invisible");
        channel.send("Shutting down");
        bot.destroy(err => {
            log(err);
        });
        process.exit(0);
        return;
    }

    let commandWithArgs = msg.replace(prefix, ""),
        invoke = commandWithArgs.split(" ", 1)[0],
        args = commandWithArgs.split(" ").slice(1);

    //Loop over the registered commands
    for(let command of commands) {
        if(command.invoke === invoke) {
            command.run(invoke, args, message, prefix);
        } else if(command.aliases) {
            for(let alias of command.aliases) {
                if(alias === invoke) {
                    command.run(invoke, args, message, prefix);
                }
            }
        }
    }
}



bot.login(config.discord.token).catch(err => {
    log(err);
});

