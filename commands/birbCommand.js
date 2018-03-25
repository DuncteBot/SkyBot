const webUtils = require("../utils/webUtils"),
    embedUtils = require("../utils/embedUtils.js");

module.exports = {
    invoke: "birb", //No spaces
    run: function (invoke, args, message, prefix) {
        message.channel.send(embedUtils.embedImage(`https://proximyst.com:4500/image/${webUtils.getText("https://proximyst.com:4500/random/path/text")}/image`))
    },
    aliases: [
        "bird"
    ],
    help: "I'm a bird"
};