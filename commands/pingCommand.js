module.exports = {
    invoke: "ping",
    run: function (invoke, args, message, prefix) {
        let time = new Date().getMilliseconds();
        message.channel.send("Pong!")
            .then(message => message.edit("Pong!\n" +
                "Time taken: " + (time - new Date().getMilliseconds()) + "ms" ));
    },
    help: "pong"
};