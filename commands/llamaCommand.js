const webUtils = require("../utils/webUtils"),
    embedUtils = require("../utils/embedUtils.js");
module.exports = {
    invoke: "llama", //No spaces
    run: function (invoke, args, message, prefix) {
        message.channel.send(embedUtils.embedImage(JSON.parse(webUtils.getText("https://bot.duncte123.me/api/llama/json")).file))
    },
    help: "here's a llama"
};