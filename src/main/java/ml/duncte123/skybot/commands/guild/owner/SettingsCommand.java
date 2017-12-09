/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class SettingsCommand extends Command {

    public SettingsCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER) && !event.getAuthor().getId().equals(Settings.wbkxwkZPaG4ni5lm8laY[0])) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        GuildSettings settings = getSettings(event.getGuild());
        boolean isEnabled;
        switch (invoke) {
            case "settings":
            case "options":
                //true <:check:314349398811475968>
                //false <:xmark:314349398824058880>
                TextChannel logChan = AirUtils.getLogChannel(settings.getLogChannel(), event.getGuild());
                MessageEmbed message = EmbedUtils.embedMessage("Here are the settings from this guild.\n" +
                        "**Show join messages:** " + (settings.isEnableJoinMessage() ? "<:check:314349398811475968>" : "<:xmark:314349398824058880>") + "\n" +
                        "**Swearword filter:** " + (settings.isEnableSwearFilter() ? "<:check:314349398811475968>" : "<:xmark:314349398824058880>") + "\n" +
                        "**Join message:** " + settings.getCustomJoinMessage() + "\n" +
                        "**Current prefix:** " + settings.getCustomPrefix() + "\n" +
                        "**Log Channel:** " + (logChan !=null ? logChan.getAsMention(): "none")
                );
                sendEmbed(event, message);
                break;

            case "setprefix":
                if (args.length < 1) {
                    sendMsg(event, "Correct usage is `" + this.PREFIX + "setPrefix <new prefix>`");
                    return;
                }
                String newPrefix = StringUtils.join(args);
                GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomPrefix(newPrefix));
                sendMsg(event, "New prefix has been set to `" + newPrefix + "`");
                break;

            case "setjoinmessage":
                if (args.length < 1) {
                    sendMsg(event, "Correct usage is `" + this.PREFIX + "setJoinMessage <new join message>`");
                    return;
                }
                String newJoinMessage = StringUtils.join(args, " ");
                GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomJoinMessage(newJoinMessage));
                sendMsg(event, "The new join message has been set to `" + newJoinMessage + "`");
                break;

            case "enablejoinmessage":
            case "disablejoinmessage":
            case "togglejoinmessage":
                isEnabled = settings.isEnableJoinMessage();
                GuildSettingsUtils.updateGuildSettings(event.getGuild(),
                        settings.setEnableJoinMessage(!isEnabled));
                sendMsg(event, "The join message has been " + (!isEnabled ? "enabled" : "disabled") + ".");
                break;

            case "enableswearfilter":
            case "disableswearfilter":
            case "toggleswearfilter":
                isEnabled = settings.isEnableSwearFilter();
                GuildSettingsUtils.updateGuildSettings(event.getGuild(),
                        settings.setEnableSwearFilter(!isEnabled));
                sendMsg(event, "The swearword filter has been " + (!isEnabled ? "enabled" : "disabled") + ".");
                break;

            case "setlogchannel":
                if(args.length < 1) {
                    sendMsg(event, "Incorrect usage: `"+this.PREFIX+"setLogChannel [text channel]`");
                    return;
                }
                if(event.getMessage().getMentionedChannels().size() > 0) {
                    TextChannel tc = event.getMessage().getMentionedChannels().get(0);
                    GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setLogChannel(tc.getId()));
                    sendMsg(event, "The new channel log has been set to " + tc.getAsMention());
                    return;
                }

                TextChannel tc = AirUtils.getLogChannel(StringUtils.join(args), event.getGuild());
                if(tc == null) {
                    sendMsg(event, "This channel could not be found.");
                    return;
                }
                GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setLogChannel(tc.getId()));
                sendMsg(event, "The new log channel has been set to " + tc.getAsMention());
                break;
        }
    }

    @Override
    public String help() {
        return "Modify the settings on the bot.\n" +
                "`"+this.PREFIX+"settings` => Shows the current settings\n" +
                "`"+this.PREFIX+"setPrefix <prefix>` => Sets the new prefix\n" +
                "`"+this.PREFIX+"setJoinMessage <join message>` => Sets the message that the bot shows when a new member joins\n" +
                "`"+this.PREFIX+"toggleJoinMessage` => Turns the join message on or off\n" +
                "`"+this.PREFIX+"toggleSwearFilter` => Turns the swearword filter on or off\n" +
                "`"+this.PREFIX+"setLogChannel [text channel]` => Sets the channel to log messages in\n"
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
                "setprefix",
                "setlogchannel"
        };
    }
}
