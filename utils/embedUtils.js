const Discord  = require("discord.js");

const defaultEmbed = function () {
  return new Discord.RichEmbed()
      .setTimestamp()
      .setFooter("SkyBot.js")
      .setColor("#0751c6");
};

const embedMessage = function (message) {
  return defaultEmbed().setDescription(message);
};

const embedImage = function (imageUrl) {
  return defaultEmbed().setImage(imageUrl);
};

module.exports = {
    defaultEmbed: defaultEmbed,
    embedMessage: embedMessage,
    embedImage: embedImage
};