function registerCommand(name) {
    return require("../commands/"+name);
}

module.exports = [
    registerCommand("helpCommand"),
    registerCommand("pingCommand"),
    registerCommand("prefixCommand"),
    registerCommand("donateCommand"),
    registerCommand("birbCommand"),
    registerCommand("llamaCommand"),
    registerCommand("alpacaCommand"),
    registerCommand("sealCommand"),
    registerCommand("catCommand"),
    registerCommand("template/templateCommand")
];