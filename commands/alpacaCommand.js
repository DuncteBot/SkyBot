const cheerio = require("cheerio"),
    embedUtils = require("../utils/embedUtils.js"),
    webUtils = require("../utils/webUtils.js");

module.exports = {
    invoke: "alpaca", //No spaces
    run: function (invoke, args, message, prefix) {
        let html = webUtils.getText("http://www.randomalpaca.com/"),
        $ = cheerio.load(html);
        message.channel.send(embedUtils.embedImage($('img').first()[0].attribs.src));
    },
    help: "Gives you an alpaca"
};