module.exports = {
    invoke: "donate", //No spaces
    run: function (invoke, args, message, prefix) {
        if(!args[0]){ message.channel.send("Here is my donation link:\n<https://paypal.me/duncte123>"); return;}
        message.channel.send("Here is my donation link:\n<https://paypal.me/duncte123/" + args[0] + "");
    },
    help: "sends me some money"
};