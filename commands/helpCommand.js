const embedUtils = require("../utils/embedUtils.js");

function commandsToEmbed(prefix, commands, embed) {

    for(let cmd of commands) {
        embed.addField(prefix + cmd.invoke, cmd.help)
    }

}

module.exports = {
    invoke: "help",
    run: function (invoke, args, message, prefix) {
        let embed = embedUtils.defaultEmbed()
            .setDescription("<name> = required argument, [name] = optional argument")
            .setTitle("Command list")
            .setURL("https://bot.duncte123.me/#commands");
        commandsToEmbed(prefix, require("../utils/commandUtils"), embed);
        message.channel.send({embed});
    },
    aliases: [
        "commands"
    ],
    help: "shows this embed"
};