const embedUtils = require("../utils/embedUtils");

module.exports = {
    invoke: "seal", //No spaces
    run: function (invoke, args, message, prefix) {
        const availableSeals = 83;
        const sealId = Math.floor(Math.random() * availableSeals) + 1;
        const idStr = ('0000' + sealId).substring(sealId.toString().length);

        message.channel.send(embedUtils.embedImage(`https://raw.githubusercontent.com/TheBITLINK/randomse.al/master/seals/${idStr}.jpg`))
    },
    aliases: [
        "zeehond"
    ],
    help: "Gives you a random seal"
};