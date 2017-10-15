/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
                .addField("Main commands", generateCommandsWithPrefix(prefix, new String[]{"help", "about", "ping", "guildinfo", "userinfo"}), INLINE)
                .addField("Music commands", generateCommandsWithPrefix(prefix, new String[]{"join", "leave", "play", "pplay", "pause", "repeat", "shuffle", "nowplaying", "skip", "stop"}), INLINE)
                .addField("Fun commands", generateCommandsWithPrefix(prefix, new String[]{"alpaca", "birb", "blob", "coin", "joke", "kpop", "seal", "kitty", "dog", "llama", "dialog", "ttb"}), INLINE)
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
