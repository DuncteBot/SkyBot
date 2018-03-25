const webUtils = require("../utils/webUtils"),
    embedUtils = require("../utils/embedUtils.js");

module.exports = {
    invoke: "cat", //No spaces
    run: function (invoke, args, message, prefix) {
        message.channel.send(embedUtils.embedImage(JSON.parse(webUtils.getText("http://random.cat/meow.php")).file))
    },
    aliases: [
        "kitty"
    ],
    help: "gives you a cat"
};