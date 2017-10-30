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
import ml.duncte123.skybot.utils.AirUtils;
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

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        if(!AirUtils.use_database) {
            sendMsg(event, "I'm sorry, but this command requires a database to be connected.");
            return;
        }

        if(!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }
        GuildSettings settings = getSettings(event.getGuild());

        if(invoke.equals("settings") || invoke.equals("options")) {
            //true ✅
            //false ❌
            MessageEmbed message = EmbedUtils.embedMessage("Here are the settings from this guild.\n" +
                    "**Show join messages:** " + (settings.isEnableJoinMessage() ? "✅" : "❌") + "\n" +
                    "**Swearword filter:** " + (settings.isEnableSwearFilter() ? "✅" : "❌") + "\n" +
                    "**Join message:** " + settings.getCustomJoinMessage() + "\n" +
                    "**Current prefix:** " + settings.getCustomPrefix()
            );
            sendEmbed(event, message);
        } else if(invoke.equals("setprefix")) {
            if(args.length < 1) {
                sendMsg(event, "Correct usage is `"+this.PREFIX+"setPrefix <new prefix>`");
                return;
            }
            String newPrefix = StringUtils.join(args);
            GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomPrefix(newPrefix));
            sendMsg(event, "New prefix has been set to `"+newPrefix+"`");
        } else if(invoke.equals("enablejoinmessage") || invoke.equals("disablejoinmessage") || invoke.equals("togglejoinmessage")) {
            boolean isEnabled = settings.isEnableJoinMessage();
            GuildSettingsUtils.updateGuildSettings(event.getGuild(),
                    settings.setEnableJoinMessage(!isEnabled));
            sendMsg(event, "The join message has been " + (isEnabled ? "enabled" : "disabled") + ".");
        } else if(invoke.equals("setjoinmessage")) {
            if(args.length < 1) {
                sendMsg(event, "Correct usage is `"+this.PREFIX+"setJoinMessage <new join message>`");
                return;
            }
            String newJoinMessage = StringUtils.join(args);
            GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomJoinMessage(newJoinMessage));
            sendMsg(event, "The new join message has been set to `"+newJoinMessage+"`");
        } else if(invoke.equals("enableswearfilter") || invoke.equals("disableswearfilter") || invoke.equals("toggleswearfilter")) {
            boolean isEnabled = settings.isEnableSwearFilter();
            GuildSettingsUtils.updateGuildSettings(event.getGuild(),
                    settings.setEnableSwearFilter(!isEnabled));
            sendMsg(event, "The swearword filter has been " + (isEnabled ? "enabled" : "disabled") + ".");
        }
    }

    @Override
    public String help() {
        return "Modify the settings on the bot.\n" +
                "`"+this.PREFIX+"settings` => Shows the current settings\n" +
                "`"+this.PREFIX+"setPrefix <prefix>` => Sets the new prefix\n" +
                "`"+this.PREFIX+"setJoinMessage <join message>` => Sets the message that the bot shows when a new member joins\n" +
                "`"+this.PREFIX+"toggleJoinMessage` => Turns the join message on or off\n" +
                "`"+this.PREFIX+"toggleSwearFilter` => Turns the swearword filter on or off\n"
                ;
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"options",
                "enablejoinmessage",
                "togglejoinmessage",
                "disablejoinmessage",
                "setjoinmessage",
                "enableswearfilter",
                "disableswearfilter",
                "toggleswearfilter",
                "setprefix"
        };
    }
}
