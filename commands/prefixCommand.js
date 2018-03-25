const settings = require("../utils/settingsUtils");
module.exports = {
    invoke: "preifx",
    run: function (invoke, args, message, prefix) {
        if(!args[0]) {
            message.channel.send(`my current prefix is \`${settings.prefixes[message.guild.id]}\`.`);
        } else {
            settings.prefixes[message.guild.id] = args.join(" ");
            message.channel.send(`prefix has been set to \`${settings.prefixes[message.guild.id]}\`.`);
        }
    },
    help: "set the prefix in this guild"
};