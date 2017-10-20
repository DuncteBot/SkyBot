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
     * @param invoke
     * @param args The command agruments
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        List<String> modules = Arrays.asList("showJoinMessage", "swearFilter", "setJoinMessage", "setPrefix");

        if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        GuildSettings settings = GuildSettingsUtils.getGuild(event.getGuild());

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
            sendMsg(event, "Incorrect usage: `" + Settings.prefix + "settings [module] [status/options]`\n\n" +
                    "The modules are: `" + StringUtils.join(modules, ", ") + "`");
        } else {
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
