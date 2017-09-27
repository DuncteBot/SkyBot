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
    public static MessageEmbed commandList = getCommandList();

    /**
     * This will return a embed containing all the commands
     * @return a embed containing all the commands
     */
    public static MessageEmbed getCommandList() {
        return getCommandListWithPrefix(Settings.prefix);
    }

    /**
     * This will return a embed containing all the commands
     * @param prefix the prefix that we need
     * @return a embed containing all the commands
     */
    public static MessageEmbed getCommandListWithPrefix(String prefix) {
        return EmbedUtils.defaultEmbed()
                .setDescription("Use `"+ prefix+"help [command]` to get more info about a command")
                .addField("Main commands", generateCommandsWithPrefix(prefix, new String[]{"help", "about", "coin", "ping", "guildinfo", "userinfo"}), INLINE)
                .addField("Music commands", generateCommandsWithPrefix(prefix, new String[]{"join", "leave", "play", "pplay", "pause", "repeat", "shuffle", "nowplaying", "skip", "stop"}), INLINE)
                .addField("Fun commands", generateCommandsWithPrefix(prefix, new String[]{"kpop", "seal", "kitty", "dog", "llama", "alpaca", "dialog", "ttb", "blob"}), INLINE)
                .addField("Mod/Admin commands", generateCommandsWithPrefix(prefix, new String[]{"ban", "clear", "softban", "unban", "kick", "settings"}), INLINE)
                .build();
    }

    /**
     * if you enter a list of commands in here it will generate a string containing all the commands
     * @param prefix The prefix that will be in frond of the commands
     * @param cmdNames the commands that should be added to the list
     * @return a concatenated string of the commands that we entered
     */
    public static String generateCommandsWithPrefix(String prefix, String[] cmdNames) {
        StringBuilder out = new StringBuilder();

        for (String name : cmdNames) {
            out.append("`").append(prefix).append(name).append("`\n");
        }

        return out.toString();
    }

    /**
     * if you enter a list of commands in here it will generate a string containing all the commands
     * @param cmdNames the commands that should be added to the list
     * @return a concatenated string of the commands that we entered
     */
    public static String generateCommands(String[] cmdNames) {
        return generateCommandsWithPrefix(Settings.prefix, cmdNames);
    }
}
