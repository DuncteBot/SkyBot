package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.SkyBot;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class HelpEmbeds {

    private static boolean INLINE = true;

    public static MessageEmbed mainCommands = AirUtils.defaultEmbed()
            .setDescription("**Main Commands!!**")
            .addField(Config.prefix + "botinfo", getHelp("botinfo"), INLINE)
            .addField(Config.prefix + "cleanup", getHelp("cleanup"), INLINE)
            .addField(Config.prefix + "coin", getHelp("coin"), INLINE)
            .addField(Config.prefix + "help", getHelp("help"), INLINE)
            .addField(Config.prefix + "ping", getHelp("ping"), INLINE)
            .addField(Config.prefix + "guildinfo", getHelp("guildinfo"), INLINE)
            .addField(Config.prefix + "userinfo [user]", getHelp("userinfo"), INLINE)
    .build();

    public static MessageEmbed musicCommands = AirUtils.defaultEmbed()
            .setDescription("**Music Commands!!**")
            .addField(Config.prefix + "join", getHelp("join"), INLINE)
            .addField(Config.prefix + "leave", getHelp("leave"), INLINE)
            .addField(Config.prefix + "play", getHelp("play"), INLINE)
            .addField(Config.prefix + "pplay", getHelp("pplay"), INLINE)
            .addField(Config.prefix + "pause", getHelp("pause"), INLINE)
            .addField(Config.prefix + "repeat", getHelp("repeat"), INLINE)
            .addField(Config.prefix + "shuffle", getHelp("shuffle"), INLINE)
            .addField(Config.prefix + "nowplaying", getHelp("nowplaying"), INLINE)
            .addField(Config.prefix + "skip", getHelp("skip"), INLINE)
            .addField(Config.prefix + "stop", getHelp("stop"), INLINE)
            .build();


    public static MessageEmbed funCommands = AirUtils.defaultEmbed()
            .setDescription("**Fun Commands!!**")
            .addField(Config.prefix + "kpop", getHelp("kpop"), INLINE)
            .addField(Config.prefix + "cat", getHelp("cat"), INLINE)
            .addField(Config.prefix + "seal", getHelp("seal"), INLINE)
            .addField(Config.prefix + "kitty", getHelp("kitty"), INLINE)
            .addField(Config.prefix + "cookie", getHelp("cookie"), INLINE)
            .addField(Config.prefix + "llama", getHelp("llama"), INLINE)
            .addField(Config.prefix + "alpaca", getHelp("alpaca"), INLINE)
            .addField(Config.prefix + "dialog", getHelp("dialog"), INLINE)
            .addField(Config.prefix + "dog", getHelp("dog"), INLINE)
            .addField(Config.prefix + "potato", getHelp("potato"), INLINE)
            .addField(Config.prefix + "spam", getHelp("spam"), INLINE)
            .addField(Config.prefix + "ttb", getHelp("ttb"), INLINE)
            .addField(Config.prefix + "trigger", getHelp("trigger"), INLINE)
            .build();

    public static MessageEmbed modCommands = AirUtils.defaultEmbed()
            .setDescription("**Mod/admin Commands!!**")
            .addField(Config.prefix + "ban", getHelp("ban"), INLINE)
            .addField(Config.prefix + "softban", getHelp("softban"), INLINE)
            .addField(Config.prefix + "unban", getHelp("unban"), INLINE)
            .addField(Config.prefix + "kick", getHelp("kick"), INLINE)
            .addField(Config.prefix + "settings", getHelp("settings"), INLINE)
            .build();

    private static String getHelp(String cmd) {
        return SkyBot.commands.get(cmd).help();
    }

}
