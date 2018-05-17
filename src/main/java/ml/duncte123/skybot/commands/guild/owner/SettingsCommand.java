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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ml.duncte123.skybot.utils.MessageUtils.*;

public class SettingsCommand extends Command {

    public SettingsCommand() {
        this.category = CommandCategory.MOD_ADMIN;
        this.displayAliasesInHelp = true;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        //noinspection deprecation
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER) && !isDev(event.getAuthor())) {
            sendMsg(event, "You need the \"Manage Server\" permission to use this command");
            return;
        }

        Guild guild = event.getGuild();
        GuildSettings settings = getSettings(guild);
        boolean isEnabled;
        switch (invoke) {
            case "settings":
            case "options": {
                //true <:check:314349398811475968>
                //false <:xmark:314349398824058880>
                TextChannel logChan = AirUtils.getLogChannel(settings.getLogChannel(), guild);
                TextChannel welcomeLeaveChannel = AirUtils.getLogChannel(settings.getWelcomeLeaveChannel(), guild);
                MessageEmbed message = EmbedUtils.embedMessage("Here are the settings from this guild.\n" +
                        "**Show join/leave messages:** " + boolToEmoji(settings.isEnableJoinMessage()) + "\n" +
                        "**Swearword filter:** " + boolToEmoji(settings.isEnableSwearFilter()) + "\n" +
                        "**Announce next track:** " + boolToEmoji(settings.isAnnounceTracks()) + "\n" +
                        "**Auto de-hoist:** " + boolToEmoji(settings.isAutoDeHoist()) + "\n" +
                        "**Filter Discord invites:** " + boolToEmoji(settings.isFilterInvites()) + "\n" +
                        "**Spamfilter:** " + boolToEmoji(settings.getSpamFilterState()) + "\n" +
                        "**Kick Mode:** " + (settings.getKickState() ? "Kick Members" : "Mute members") + "\n" +
                        "**MuteRole:** " + (settings.getMuteRoleId() == null || settings.getMuteRoleId().equals("")
                        ? "Not Set" : guild.getRoleById(settings.getMuteRoleId()).getAsMention()) + "\n" +
                        "**Join message:** " + settings.getCustomJoinMessage() + "\n" +
                        "**Leave message:** " + settings.getCustomLeaveMessage() + "\n" +
                        "**AutoRole:** " + (settings.getAutoroleRole() == null || settings.getAutoroleRole().equals("")
                        ? "Not Set" : guild.getRoleById(settings.getAutoroleRole()).getAsMention()) + "\n" +
                        "**Current prefix:** " + settings.getCustomPrefix() + "\n" +
                        "**Modlog Channel:** " + (logChan != null ? logChan.getAsMention() : "none") + "\n" +
                        "**Welcome/Leave channel:** " + (welcomeLeaveChannel != null ? welcomeLeaveChannel.getAsMention() : "none")
                );
                sendEmbed(event, message);
                break;
            }

            case "setprefix":
                if (args.length < 1) {
                    sendMsg(event, "Correct usage is `" + PREFIX + "setPrefix <new prefix>`");
                    return;
                }
                String newPrefix = StringUtils.join(args, " ");
                GuildSettingsUtils.updateGuildSettings(guild, settings.setCustomPrefix(newPrefix));
                sendMsg(event, "New prefix has been set to `" + newPrefix + "`");
                break;

            case "setjoinmessage":
            case "setwelcomenmessage":
                if (args.length < 1) {
                    sendMsg(event, "Correct usage is `" + PREFIX + "setJoinMessage <new join message>`");
                    return;
                }
                String newJoinMessage = event.getMessage().getContentRaw().split("\\s+", 2)[1].replaceAll("\n", "\\\\n")/*.replaceAll("\n", "\r\n")*/;
                GuildSettingsUtils.updateGuildSettings(guild, settings.setCustomJoinMessage(newJoinMessage));
                sendMsg(event, "The new join message has been set to `" + newJoinMessage + "`");
                break;

            case "setleavemessage":
                if (args.length < 1) {
                    sendMsg(event, "Correct usage is `" + PREFIX + "setleavemessage <new join message>`");
                    return;
                }
                String newLeaveMessage = event.getMessage().getContentRaw().split("\\s+", 2)[1].replaceAll("\n", "\\\\n")/*.replaceAll("\n", "\r\n")*/;
                GuildSettingsUtils.updateGuildSettings(guild, settings.setCustomLeaveMessage(newLeaveMessage));
                sendMsg(event, "The new leave message has been set to `" + newLeaveMessage + "`");
                break;

            case "enablejoinmessage":
            case "disablejoinmessage":
            case "togglejoinmessage":
                isEnabled = settings.isEnableJoinMessage();
                GuildSettingsUtils.updateGuildSettings(guild,
                        settings.setEnableJoinMessage(!isEnabled));
                sendMsg(event, "The join and leave messages have been " + (!isEnabled ? "enabled" : "disabled") + ".");
                break;

            case "enableswearfilter":
            case "disableswearfilter":
            case "toggleswearfilter":
                isEnabled = settings.isEnableSwearFilter();
                GuildSettingsUtils.updateGuildSettings(guild,
                        settings.setEnableSwearFilter(!isEnabled));
                sendMsg(event, "The swearword filter has been " + (!isEnabled ? "enabled" : "disabled") + ".");
                break;

            case "setlogchannel":
                if (args.length < 1) {
                    sendMsg(event, "Incorrect usage: `" + PREFIX + "setLogChannel [text channel]`");
                    return;
                }
                if (event.getMessage().getMentionedChannels().size() > 0) {
                    TextChannel tc = event.getMessage().getMentionedChannels().get(0);
                    if (!tc.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) {
                        sendError(event.getMessage());
                        sendMsg(event, "I'm sorry but I have to be able to talk in that channel.");
                        return;
                    }
                    GuildSettingsUtils.updateGuildSettings(guild, settings.setLogChannel(tc.getId()));
                    sendMsg(event, "The new log channel has been set to " + tc.getAsMention());
                    return;
                }

                TextChannel tc = AirUtils.getLogChannel(StringUtils.join(args), guild);
                if (tc == null) {
                    sendMsg(event, "This channel could not be found.");
                    return;
                }
                GuildSettingsUtils.updateGuildSettings(guild, settings.setLogChannel(tc.getId()));
                sendMsg(event, "The new log channel has been set to " + tc.getAsMention());
                break;
            case "setwelcomechannel":
            case "setleavechannel":
                if (args.length < 1) {
                    sendMsg(event, "Incorrect usage: `" + PREFIX + "setwelcomechannel [text channel]`");
                    return;
                }
                if (event.getMessage().getMentionedChannels().size() > 0) {
                    TextChannel welcomeChannel = event.getMessage().getMentionedChannels().get(0);
                    if (!welcomeChannel.getGuild().getSelfMember().hasPermission(welcomeChannel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) {
                        sendError(event.getMessage());
                        sendMsg(event, "I'm sorry but I have to be able to talk in that channel.");
                        return;
                    }
                    GuildSettingsUtils.updateGuildSettings(guild, settings.setWelcomeLeaveChannel(welcomeChannel.getId()));
                    sendMsg(event, "The new welcome channel has been set to " + welcomeChannel.getAsMention());
                    return;
                }

                TextChannel welcomeChannel = AirUtils.getLogChannel(StringUtils.join(args), guild);
                if (welcomeChannel == null) {
                    sendMsg(event, "This channel could not be found.");
                    return;
                }
                GuildSettingsUtils.updateGuildSettings(guild, settings.setWelcomeLeaveChannel(welcomeChannel.getId()));
                sendMsg(event, "The new welcome channel has been set to " + welcomeChannel.getAsMention());
                break;

            case "autorole": {

                if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                    sendMsg(event, "I need the _Manage Roles_ permission in order for this feature to work.");
                    return;
                }

                if (args.length == 0) {
                    sendMsg(event, "Incorrect usage: `" + PREFIX + "autorole <role name/disable>`");
                    return;
                }

                if ("disable".equals(args[0])) {
                    sendMsg(event, "AutoRole feature has been disabled");
                    GuildSettingsUtils.updateGuildSettings(guild, settings.setAutoroleRole(""));
                    return;
                }

                List<Role> rolesFound = guild.getRolesByName(StringUtils.join(args, " "), true);

                if (!roleCheck(event, rolesFound)) return;
                if (rolesFound.get(0).getPosition() >= guild.getSelfMember().getRoles().get(0).getPosition()) {
                    sendMsg(event, "I'm sorry but I can't give that role to people, move my role above the role and try again.");
                    return;
                }

                GuildSettingsUtils.updateGuildSettings(guild, settings.setAutoroleRole(rolesFound.get(0).getId()));
                sendMsg(event, "AutoRole has been set to " + rolesFound.get(0).getAsMention());

                break;
            }

            case "setdescription":
                if (args.length < 1) {
                    sendError(event.getMessage());
                    sendMsg(event, "Incorrect usage\n" +
                            "Correct usage : `" + PREFIX + invoke + " <description>`");
                    return;
                } else if ("null".equals(args[0])) {
                    GuildSettingsUtils.updateGuildSettings(guild, settings.setServerDesc(null));
                    sendMsg(event, "Description has been reset.");
                    return;
                }
                String description = event.getMessage().getContentRaw().split("\\s+", 2)[1].replaceAll("\n", "\\\\n");
                GuildSettingsUtils.updateGuildSettings(guild, settings.setServerDesc(description));
                sendMsg(event, "Description has been updated, check `" + PREFIX + "guildinfo` to see your description");
                break;

            case "toggleannouncetracks":
                boolean shouldAnnounceTracks = !settings.isAnnounceTracks();
                GuildSettingsUtils.updateGuildSettings(guild, settings.setAnnounceTracks(shouldAnnounceTracks));
                sendMsg(event, "Announcing the next track has been **"
                        + (shouldAnnounceTracks ? "enabled" : "disabled") + "**");
                break;

            case "togglefilterinvites":
                boolean shouldFilterInvites = !settings.isFilterInvites();
                GuildSettingsUtils.updateGuildSettings(guild, settings.setFilterInvites(shouldFilterInvites));
                sendMsg(event, "Filtering discord invites has been **"
                        + (shouldFilterInvites ? "enabled" : "disabled") + "**");
                break;

            case "toggleautodehoist":
                boolean shouldAutoDeHoist = !settings.isAutoDeHoist();
                GuildSettingsUtils.updateGuildSettings(guild, settings.setAutoDeHoist(shouldAutoDeHoist));
                sendMsg(event, "Auto de-hoisting has been **"
                        + (shouldAutoDeHoist ? "enabled" : "disabled") + "**");
                break;

            case "togglespamfilter":
                boolean spamState = !settings.getSpamFilterState();
                GuildSettingsUtils.updateGuildSettings(guild, settings.setSpamFilterState(spamState));
                String message = String.format("Spamfilter **%s**!", (spamState ? "activated" : "disabled"));
                String muteRoleId = settings.getMuteRoleId();
                if (muteRoleId == null || muteRoleId.isEmpty()) {
                    message = "**__Please set a spam/mute role first!__**";
                } else {
                    Role r = guild.getRoleById(muteRoleId);
                    message += "\nThe spam rule is " + ((r == null) ? "deleted. Please update it." : r.getName() + ". Change it if it's outdated.");
                }
                sendMsg(event, message);
                break;

            case "spamrole":

                if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                    sendMsg(event, "I need the _Manage Roles_ permission in order for this feature to work.");
                    return;
                }

                if (args.length == 0) {
                    sendMsg(event, "Incorrect usage: `" + PREFIX + "spamrole <role name/disable>`");
                    return;
                }

                if ("disable".equals(args[0])) {
                    sendMsg(event, "SpamRole feature & SpamFilter has been disabled");
                    //Never clean the role's id so activating the filter wont cause issues.
                    //GuildSettingsUtils.updateGuildSettings(guild, settings.setMuteRoleId(""));
                    GuildSettingsUtils.updateGuildSettings(guild, settings.setSpamFilterState(false));
                    return;
                }

                List<Role> rolesFound = guild.getRolesByName(StringUtils.join(args, " "), true);

                if (!roleCheck(event, rolesFound)) return;
                if (rolesFound.get(0).getPosition() >= guild.getSelfMember().getRoles().get(0).getPosition()) {
                    sendMsg(event, "I'm sorry but I can't give that role to people, move my role above the role and try again.");
                    return;
                }

                GuildSettingsUtils.updateGuildSettings(guild, settings.setMuteRoleId(rolesFound.get(0).getId()));
                sendMsg(event, "SpamRole has been set to " + rolesFound.get(0).getAsMention());

                break;
                
            case "togglekickmode":
                boolean kickState = !settings.getKickState();
                GuildSettingsUtils.updateGuildSettings(guild, settings.setKickState(kickState));
                sendMsg(event, "Kick-Mode **" + (kickState ? "activated" : "disabled") + "**!");
                break;
                
            case "setratelimits":
                
                if (args.length == 0) {
                    sendMsg(event, "Incorrect usage: `" + PREFIX + "setratelimits <1|2|3|4|5|6/default>`");
                    return;
                }

                if ("default".equals(args[0])) {
                    sendMsg(event, "Ratelimits have beed reset.");
                    GuildSettingsUtils.updateGuildSettings(guild, settings.setRatelimits(new long[] {20, 45, 60, 120, 240, 2400}));
                    return;
                }

                long[] rates = GuildSettingsUtils.ratelimmitChecks(args[0]);
                GuildSettingsUtils.updateGuildSettings(guild, settings.setRatelimits(rates));
                String steps = Arrays.stream(rates).mapToObj(String::valueOf).collect(Collectors.joining(", ", "", " minutes"));
                sendMsg(event, "The new rates are " + steps);
                break;
        }
    }

    private boolean roleCheck(GuildMessageReceivedEvent event, List<Role> rolesFound) {
        if (rolesFound.size() == 0) {
            if (event.getMessage().getMentionedRoles().size() > 0) {
                rolesFound.add(event.getMessage().getMentionedRoles().get(0));
            } else {
                sendMsg(event, "I could not find any roles with that name");
                return false;
            }
        }
        return true;
    }

    @Override
    public String help(String invoke) {
        switch (invoke) {
            case "settings":
            case "options":
                return "Shows the current settings\n" +
                        "Usage: `" + PREFIX + invoke + "`";
            case "setprefix":
                return "Sets the new prefix\n" +
                        "Usage: `" + PREFIX + invoke + " <prefix>`";
            case "setjoinmessage":
            case "setwelcomenmessage":
                return "Sets the message that the bot shows when a new member joins\n" +
                        "Usage: `" + PREFIX + invoke + " <join message>`";
            case "setleavemessage":
                return "Sets the message that the bot shows when a member leaves\n" +
                        "Usage: `" + PREFIX + invoke + " <leave message>`";
            case "enablejoinmessage":
            case "disablejoinmessage":
            case "togglejoinmessage":
                return "Turns the join message on or off\n" +
                        "Usage: `" + PREFIX + invoke + "`";
            case "enableswearfilter":
            case "disableswearfilter":
            case "toggleswearfilter":
                return "Turns the swearword filter on or off\n" +
                        "Usage: `" + PREFIX + invoke + "`";
            case "setlogchannel":
                return "Sets the channel to log messages in\n" +
                        "Usage: `" + PREFIX + invoke + " <text channel>`";
            case "setwelcomechannel":
            case "setleavechannel":
                return "Sets the channel that displays the welcome and leave messages\n" +
                        "Usage: `" + PREFIX + invoke + " <channel>`";
            case "autorole":
                return "Gives members a role when they join\n" +
                        "Usage: `" + PREFIX + invoke + " <role>`";
            case "setdescription":
                return "Set a custom description in " + PREFIX + "guildinfo\n" +
                        "Usage: `" + PREFIX + invoke + " <desc>`";
            case "toggleannouncetracks":
                return "Toggles if the player should announce the next playing track\n" +
                        "Usage: `" + PREFIX + invoke + "`";
            case "togglefilterinvites":
                return "Toggles if the bot should delete messages that contain invites\n" +
                        "Usage: `" + PREFIX + invoke + "`";
            case "toggleautodehoist":
                return "Toggles if if the bot should auto de-hoist users\n" +
                        "Usage: `" + PREFIX + invoke + "`";
            case "togglespamfilter":
                return "Toggles whether we should handle your incoming spam.\n" +
                        "Usage: `" + PREFIX + invoke + "`";
            case "spamrole":
                return "Gives members a role when they spam.\n" +
                        "Usage: `" + PREFIX + invoke + " <role>`";
            case "setratelimits":
                return "Sets our cooldown for un-muting your spammer of choice.\n" +
                        "Usage: `" + PREFIX + invoke + " <1|2|3|4|5|6>`\n" +
                        "Example: " + PREFIX + invoke + "20|45|60|120|240|2400";
            case "togglekickmode":
                return "Toggles whether we should kick or assign a role for spammers.\n" +
                        "Usage: `" + PREFIX + invoke + "`";

            default:
                return "invalid invoke";
        }
    }

    @Override
    public String help() {
        return "Modify the settings on the bot.\n" +
                "`" + PREFIX + "settings` => Shows the current settings.\n" +
                "`" + PREFIX + "setPrefix <prefix>` => Sets the new prefix.\n" +
                "`" + PREFIX + "setJoinMessage <join message>` => Sets the message that the bot shows when a new member joins.\n" +
                "`" + PREFIX + "setLeaveMessage <leave message>` => Sets the message that the bot shows when a member leaves.\n" +
                "`" + PREFIX + "toggleJoinMessage` => Turns the join message on or off.\n" +
                "`" + PREFIX + "toggleSwearFilter` => Turns the swearword filter on or off.\n" +
                "`" + PREFIX + "setLogChannel <text channel>` => Sets the channel to log messages in.\n" +
                "`" + PREFIX + "setWelcomeChannel <channel>` => Sets the channel that displays the welcome and leave messages.\n" +
                "`" + PREFIX + "autorole <role>` => Gives members a role when they join.\n" +
                "`" + PREFIX + "setdescription <desc>` => Set a custom description in " + PREFIX + "guildinfo.\n" +
                "`" + PREFIX + "toggleannouncetracks` => Toggles if the player should announce the next playing track.\n" +
                "`" + PREFIX + "togglefilterinvites` => Toggles if the bot should delete messages that contain invites.\n" +
                "`" + PREFIX + "toggleautodehoist` => Toggles if if the bot should auto de-hoist users.\n" +
                "`" + PREFIX + "togglespamfilter` => Toggles whether we should handle your incoming spam.\n" +
                "`" + PREFIX + "spamrole <role>` => Gives members a role when they spam.\n" +
                "`" + PREFIX + "setratelimits <1|2|3|4|5|6>` => Sets our cooldown for un-muting your spammer of choice.\n" +
                "`" + PREFIX + "togglekickmode` => Toggles whether we should kick or assign a role for spammers.\n"
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
                "autorole",
                "setdescription",
                "toggleannouncetracks",
                "togglefilterinvites",
                "toggleautodehoist",
                "togglespamfilter",
                "spamrole",
                "setratelimits",
                "togglekickmode"
        };
    }


    private String boolToEmoji(boolean flag) {
        return flag ? "<:check:414777605141561344>" : "<:xmark:414777605250875402>";
    }
}
