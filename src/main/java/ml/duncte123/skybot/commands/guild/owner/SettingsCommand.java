/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static ml.duncte123.skybot.utils.MessageUtils.*;

public class SettingsCommand extends Command {

    public SettingsCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        //noinspection deprecation
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
                TextChannel welcomeLeaveChannel = AirUtils.getLogChannel(settings.getWelcomeLeaveChannel(), event.getGuild());
                MessageEmbed message = EmbedUtils.embedMessage("Here are the settings from this guild.\n" +
                        "**Show join/leave messages:** " + (settings.isEnableJoinMessage() ? "<:check:314349398811475968>" : "<:xmark:314349398824058880>") + "\n" +
                        "**Swearword filter:** " + (settings.isEnableSwearFilter() ? "<:check:314349398811475968>" : "<:xmark:314349398824058880>") + "\n" +
                        "**Join message:** " + settings.getCustomJoinMessage() + "\n" +
                        "**Leave message:** " + settings.getCustomLeaveMessage() + "\n" +
                        "**AutoRole:** " + (settings.getAutoroleRole() == null || settings.getAutoroleRole().equals("") ? "Not Set": event.getGuild().getRoleById(settings.getAutoroleRole()).getAsMention() )+ "\n" +
                        "**Current prefix:** " + settings.getCustomPrefix() + "\n" +
                        "**Modlog Channel:** " + (logChan !=null ? logChan.getAsMention(): "none") + "\n" +
                        "**Welcome/Leave channel:** " + (welcomeLeaveChannel != null ? welcomeLeaveChannel.getAsMention() : "none")
                );
                sendEmbed(event, message);
                break;

            case "setprefix":
                if (args.length < 1) {
                    sendMsg(event, "Correct usage is `" + PREFIX + "setPrefix <new prefix>`");
                    return;
                }
                String newPrefix = StringUtils.join(args, " ");
                GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomPrefix(newPrefix));
                sendMsg(event, "New prefix has been set to `" + newPrefix + "`");
                break;

            case "setjoinmessage":
            case "setwelcomenmessage":
                if (args.length < 1) {
                    sendMsg(event, "Correct usage is `" + PREFIX + "setJoinMessage <new join message>`");
                    return;
                }
                String newJoinMessage = event.getMessage().getContentRaw().split("\\s+",2)[1].replaceAll("\\\\n","\n").replaceAll("\n", "\r\n");
                GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomJoinMessage(newJoinMessage));
                sendMsg(event, "The new join message has been set to `" + newJoinMessage + "`");
                break;

            case "setleavemessage":
                if (args.length < 1) {
                    sendMsg(event, "Correct usage is `" + PREFIX + "setleavemessage <new join message>`");
                    return;
                }
                String newLeaveMessage = event.getMessage().getContentRaw().split("\\s+",2)[1].replaceAll("\\\\n","\n").replaceAll("\n", "\r\n");
                GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomLeaveMessage(newLeaveMessage));
                sendMsg(event, "The new leave message has been set to `" + newLeaveMessage + "`");
                break;

            case "enablejoinmessage":
            case "disablejoinmessage":
            case "togglejoinmessage":
                isEnabled = settings.isEnableJoinMessage();
                GuildSettingsUtils.updateGuildSettings(event.getGuild(),
                        settings.setEnableJoinMessage(!isEnabled));
                sendMsg(event, "The join and leave messages have been " + (!isEnabled ? "enabled" : "disabled") + ".");
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
                    sendMsg(event, "Incorrect usage: `"+PREFIX+"setLogChannel [text channel]`");
                    return;
                }
                if(event.getMessage().getMentionedChannels().size() > 0) {
                    TextChannel tc = event.getMessage().getMentionedChannels().get(0);
                    if(!tc.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) {
                        sendError(event.getMessage());
                        sendMsg(event, "I'm sorry but I have to be able to talk in that channel.");
                        return;
                    }
                    GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setLogChannel(tc.getId()));
                    sendMsg(event, "The new log channel has been set to " + tc.getAsMention());
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
            case "setwelcomechannel":
            case "setleavechannel":
                if(args.length < 1) {
                    sendMsg(event, "Incorrect usage: `"+PREFIX+"setwelcomechannel [text channel]`");
                    return;
                }
                if(event.getMessage().getMentionedChannels().size() > 0) {
                    TextChannel welcomeChannel = event.getMessage().getMentionedChannels().get(0);
                    if(!welcomeChannel.getGuild().getSelfMember().hasPermission(welcomeChannel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) {
                        sendError(event.getMessage());
                        sendMsg(event, "I'm sorry but I have to be able to talk in that channel.");
                        return;
                    }
                    GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setWelcomeLeaveChannel(welcomeChannel.getId()));
                    sendMsg(event, "The new welcome channel has been set to " + welcomeChannel.getAsMention());
                    return;
                }

                TextChannel welcomeChannel = AirUtils.getLogChannel(StringUtils.join(args), event.getGuild());
                if(welcomeChannel == null) {
                    sendMsg(event, "This channel could not be found.");
                    return;
                }
                GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setWelcomeLeaveChannel(welcomeChannel.getId()));
                sendMsg(event, "The new welcome channel has been set to " + welcomeChannel.getAsMention());
                break;

            case "autorole":

                if(!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                    sendMsg(event, "I need the _Manage Roles_ permission in order for this feature to work.");
                    return;
                }

                if(args.length == 0) {
                    sendMsg(event, "Incorrect usage: `"+PREFIX+"autorole <role name/disable>`");
                    return;
                }

                if("disable".equals(args[0])) {
                    sendMsg(event, "AutoRole feature has been disabled");
                    GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setAutoroleRole(""));
                    return;
                }

                List<Role> rolesFound = event.getGuild().getRolesByName(StringUtils.join(args, " "), true);

                if(rolesFound.size() == 0) {
                    sendMsg(event, "I could not find any roles with that name");
                    return;
                }
                if(rolesFound.get(0).getPosition() >= event.getGuild().getSelfMember().getRoles().get(0).getPosition()) {
                    sendMsg(event, "I'm sorry but I can't give that role to people, move my role above the role and try again.");
                    return;
                }

                GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setAutoroleRole(rolesFound.get(0).getId()));
                sendMsg(event, "AutoRole has been set to " + rolesFound.get(0).getAsMention());

                break;
        }
    }

    @Override
    public String help() {
        return "Modify the settings on the bot.\n" +
                "`"+PREFIX+"settings` => Shows the current settings\n" +
                "`"+PREFIX+"setPrefix <prefix>` => Sets the new prefix\n" +
                "`"+PREFIX+"setJoinMessage <join message>` => Sets the message that the bot shows when a new member joins\n" +
                "`"+PREFIX+"setLeaveMessage <leave message>` => Sets the message that the bot shows when a member leaves\n" +
                "`"+PREFIX+"toggleJoinMessage` => Turns the join message on or off\n" +
                "`"+PREFIX+"toggleSwearFilter` => Turns the swearword filter on or off\n" +
                "`"+PREFIX+"setLogChannel <text channel>` => Sets the channel to log messages in\n" +
                "`"+PREFIX+"setWelcomeChannel <channel>` => Sets the channel that displays the welcome and leave messages\n" +
                "`"+PREFIX+"autorole <role>` => Gives members a role when they join"
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
                "setwelcomenmessage",
                "enableswearfilter",
                "disableswearfilter",
                "toggleswearfilter",
                "setprefix",
                "setlogchannel",
                "setwelcomechannel",
                "setleavechannel",
                "setleavemessage",
                "autorole"
        };
    }
}
