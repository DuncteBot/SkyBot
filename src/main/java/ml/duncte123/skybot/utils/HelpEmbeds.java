package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.SkyBot;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class HelpEmbeds {

    public static MessageEmbed mainCommands = AirUtils.defaultEmbed()
            .setDescription("**Main Commands!!**")
            .addField(Config.prefix + "botinfo", getHelp("botinfo"), false)
            .addField(Config.prefix + "cleanup", getHelp("cleanup"), false)
            .addField(Config.prefix + "coin", getHelp("coin"), false)
            .addField(Config.prefix + "help", getHelp("help"), false)
            .addField(Config.prefix + "ping", getHelp("ping"), false)
            .addField(Config.prefix + "guildinfo", getHelp("guildinfo"), false)
            .addField(Config.prefix + "userinfo [user]", getHelp("userinfo"), false)
    .build();

    public static MessageEmbed musicCommands = AirUtils.defaultEmbed()
            .setDescription("**Music Commands!!**")
            .addField(Config.prefix + "join", getHelp("join"), false)
            .addField(Config.prefix + "leave", getHelp("leave"), false)
            .addField(Config.prefix + "play", getHelp("play"), false)
            .addField(Config.prefix + "pplay", getHelp("pplay"), false)
            .addField(Config.prefix + "pause", getHelp("pause"), false)
            .addField(Config.prefix + "repeat", getHelp("repeat"), false)
            .addField(Config.prefix + "shuffle", getHelp("shuffle"), false)
            .addField(Config.prefix + "nowplaying", getHelp("nowplaying"), false)
            .addField(Config.prefix + "skip", getHelp("skip"), false)
            .addField(Config.prefix + "stop", getHelp("stop"), false)
            .build();


    public static MessageEmbed funCommands = AirUtils.defaultEmbed()
            .setDescription("**Fun Commands!!**")
            .addField(Config.prefix + "kpop", getHelp("kpop"), false)
            .addField(Config.prefix + "cat", getHelp("cat"), false)
            .addField(Config.prefix + "kitty", getHelp("kitty"), false)
            .addField(Config.prefix + "cookie", getHelp("cookie"), false)
            .addField(Config.prefix + "dialog", getHelp("dialog"), false)
            .addField(Config.prefix + "dog", getHelp("dog"), false)
            .addField(Config.prefix + "mineh", getHelp("mineh"), false)
            .addField(Config.prefix + "potato", getHelp("potato"), false)
            .addField(Config.prefix + "spam", getHelp("spam"), false)
            .addField(Config.prefix + "ttb", getHelp("ttb"), false)
            .addField(Config.prefix + "trigger", getHelp("trigger"), false)
            .build();

    public static MessageEmbed modCommands = AirUtils.defaultEmbed()
            .setDescription("**Mod Commands!!**")
            .addField(Config.prefix + "ban", getHelp("ban"), false)
            .addField(Config.prefix + "softban", getHelp("softban"), false)
            .addField(Config.prefix + "unban", getHelp("unban"), false)
            .addField(Config.prefix + "kick", getHelp("kick"), false)
            .build();

    private static String getHelp(String cmd) {
        return SkyBot.commands.get(cmd).help();
    }

}
