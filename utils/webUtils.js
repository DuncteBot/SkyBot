const XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest,
    log = require("fancy-log");

// snekfetch.post(`https://discordbots.org/api/bots/${bot.user.id}/stats`)
//     .set('Authorization', key)
//     .send({ server_count: bot.guilds.size,
//         shard_count: bot.shard.count,
//         shard_id: bot.shard.id })
//     .then(() => console.log(`Posted to dbl.`))
//     .catch((e) => console.error(e));

const getRequest = function (url) {
    let ajax = new XMLHttpRequest();
    ajax.open("GET", url, false);
    ajax.onreadystatechange = () => {
        //log(ajax.readyState);
    };
    ajax.send(null);
    return ajax;
};

const getText = function (url) {
    return getRequest(url).responseText;
};

module.exports = {
    getRequest: getRequest,
    getText: getText
};