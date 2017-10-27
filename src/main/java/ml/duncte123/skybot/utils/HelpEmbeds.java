/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
                .addField("Main commands", generateCommandsWithPrefix(prefix, new String[]{"help", "about", "ping", "guildinfo", "userinfo", "alpha"}), INLINE)
                .addField("Music commands", generateCommandsWithPrefix(prefix, new String[]{"join", "leave", "play", "pplay", "pause", "repeat", "shuffle", "nowplaying", "skip", "stop"}), INLINE)
                .addField("Fun commands", generateCommandsWithPrefix(prefix, new String[]{"alpaca", "birb", "blob", "coin", "joke", "kpop", "seal", "kitty", "dog", "llama", "dialog", "ttb"}), INLINE)
                .addField("Mod/Admin commands", generateCommandsWithPrefix(prefix, new String[]{"announce", "ban", "clear", "softban", "unban", "kick", "settings"}), INLINE)
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
