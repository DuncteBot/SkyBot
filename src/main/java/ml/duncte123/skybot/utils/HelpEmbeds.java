package ml.duncte123.skybot.utils;

import net.dv8tion.jda.core.entities.MessageEmbed;

public class HelpEmbeds {

    /**
     * This tells the fields to be inline or not
     */
    private static boolean INLINE = false;

    /**
     * This is the embed containing all the commands
     */
    public static MessageEmbed commandList = EmbedUtils.defaultEmbed()
            .setDescription("Use `"+ Settings.prefix+"help [command]` to get more info about a command")
            .addField("Main commands", generateCommands(new String[]{"help", "about", "clear", "coin", "ping", "guildinfo", "userinfo"}), INLINE)
            .addField("Music commands", generateCommands(new String[]{"join", "leave", "play", "pplay", "pause", "repeat", "shuffle", "nowplaying", "skip", "stop"}), INLINE)
            .addField("Fun commands", generateCommands(new String[]{"kpop", "seal", "kitty", "dog", "cookie", "llama", "alpaca", "dialog", "ttb", "blob"}), INLINE)
            .addField("Mod/Admin commands", generateCommands(new String[]{"ban", "softban", "unban", "kick", "settings"}), INLINE)
            .build();

    /**
     * if you enter a list of commands in here it will generate a string containing all the co
     * @param cmdNames the commands that should be added to the list
     * @return a concatenated string of the commands that we entered
     */
    private static String generateCommands(String[] cmdNames) {

        StringBuilder out = new StringBuilder();

        for (String name : cmdNames) {
            out.append("`").append(Settings.prefix).append(name).append("`\n");
        }

        return out.toString();
    }

}
