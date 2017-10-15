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

package ml.duncte123.skybot.commands.guild.owner;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SettingsCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        GuildSettings settings = getSettings(event.getGuild().getId());

        if(args.length < 1) {
            //true ✅
            //false ❌
            MessageEmbed message = EmbedUtils.embedMessage("Here are the settings from this guild.\n" +
                            "**Show join messages:** " + (settings.isEnableJoinMessage() ? "✅" : "❌") + "\n" +
                            "**Swearword filter:** " + (settings.isEnableSwearFilter() ? "✅" : "❌") + "\n" +
                            "**Join message:** " + settings.getCustomJoinMessage() + "\n" +
                            "**Current prefix:** " + settings.getCustomPrefix()
            );
            sendEmbed(event, message);
        } else if(args.length == 1) {
            sendMsg(event, "Incorrect usage: `" + Settings.prefix + "settings [module] [status/options]`");
        } else {
            List<String> modules = Arrays.asList("showJoinMessage", "swearFilter", "setJoinMessage", "setPrefix");
            String module = args[0];
            if(modules.contains(module)) {
                switch (module) {
                    case "showJoinMessage" :
                        GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setEnableJoinMessage(checkStatus(args[1])));
                        break;
                    case "swearFilter":
                        GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setEnableSwearFilter(checkStatus(args[1])));
                        break;
                    case "setJoinMessage":
                        String newJoinMsg = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
                        GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomJoinMessage(newJoinMsg));
                        break;
                    case "setPrefix":
                        String newPrefix = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
                        GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomPrefix(newPrefix));
                        break;

                    default:
                        return;
                }
                sendEmbed(event, EmbedUtils.embedMessage("Settings have been updated."));

            } else {
                sendMsg(event, "Module has not been reconsigned, please choose from: `" + StringUtils.join(modules, ", ") + "`");
            }
        }

    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Modify the settings on the bot";
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"options"};
    }

    private boolean checkStatus(String toCHeck) {
        try {
            return (Integer.parseInt(toCHeck) >= 1);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
