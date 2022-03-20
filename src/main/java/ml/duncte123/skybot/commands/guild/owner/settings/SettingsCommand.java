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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.guild.owner.settings;

import com.dunctebot.models.settings.GuildSetting;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.duncte123.botcommons.StringUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.EventManager;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.extensions.StringKt;
import ml.duncte123.skybot.objects.TriConsumer;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dunctebot.models.utils.Utils.ratelimmitChecks;
import static me.duncte123.botcommons.messaging.MessageUtils.*;
import static ml.duncte123.skybot.extensions.BooleanKt.toEmoji;
import static ml.duncte123.skybot.utils.AirUtils.colorToHex;
import static ml.duncte123.skybot.utils.AirUtils.colorToInt;
import static ml.duncte123.skybot.utils.CommandUtils.isDev;

@SuppressWarnings("PMD.UnusedFormalParameter")
public class SettingsCommand extends Command {
    public static final Pattern COLOR_REGEX = Pattern.compile("#[a-zA-Z0-9]{6}");

    private final Map<String, TriConsumer<CommandContext, String, Boolean>> settingsMap = new ConcurrentHashMap<>();

    public SettingsCommand() {
        this.displayAliasesInHelp = true;
        // Override category here to make sure that we can hide all the other settings commands
        this.category = CommandCategory.ADMINISTRATION;
        this.name = "settings";
        this.aliases = new String[]{
            "setting",
            "options",
        };
        this.help = "Shows the current settings for this server";
        this.usage = "[item] [--set value]";
        this.extraInfo = """
            Available items are as follows:
            \u2022 `autoRole`: Sets the role given to users on join, set value is a role
            \u2022 `muteRole`: Sets the role given to users when they get muted, set value is a role
            \u2022 `embedColor`: Sets the color of the embeds DuncteBot sends, set value is a valid hex color
            \u2022 `description`: Sets the description in the server info command, set value is a piece of text
            \u2022 `joinMessage`: Sets the message being send to `welcomeChannel` when a user joins, set value is the message
            \u2022 `leaveMessage`: Sets the message being send to `welcomeChannel` when a user leaves, set value is the message
            \u2022 `logChannel`: Sets the channel where moderation actions are logged, set value is a text channel
            \u2022 `prefix`: Sets a custom prefix for the bot, set value is your desired prefix
            \u2022 `rateLimits`: Sets the cooldown in minutes for un-muting your spammer of choice, set value is in the format `1|2|3|4|5|6` or `default`
            \u2022 `welcomeChannel`: Sets the channel where the join and leave messages are send, set value is a text channel
            \u2022 `announceTracks`: Toggles the announcing of the next playing track on or off, this item has no set value
            \u2022 `autoDehoist`: Toggles the enabled state of auto de-hoisting, this item has no set value
            \u2022 `filterInvites`: Toggles if the bot should filter discord invites for messages, this item has no set value
            \u2022 `joinMessageState`: Toggles the join and leave messages on or off, this item has no set value
            \u2022 `kickMode`: Toggles the kick mode for spammers between muting and kicking, this item has no set value
            \u2022 `spamFilter`: Toggles the spam filter on or off, this item has no set value
            \u2022 `swearFilter`: Toggles the swear filter on or off, this item has no set value
            \u2022 `inviteLogging`: Toggles the logging of invite usage on or off, this item has no set value
            \u2022 `memberLogging`: Toggles the logging of members joining and leaving on or off, this item has no set value
            """;
        this.userPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
        };
        this.flags = new Flag[] {
            new Flag(
                "set",
                "Sets the value for this item"
            ),
        };

        this.loadSettingsMap();
    }

    // This is to allow developers to execute the commands as well, regardless of permissions
    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        if (isDev(ctx.getAuthor())) {
            execute(ctx);
        } else {
            super.executeCommand(ctx);
        }
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            showSettingsOverview(ctx);
            return;
        }

        final String item = args.get(0);

        if (!this.settingsMap.containsKey(item)) {
            sendMsg(ctx, "I do not know what `" + item + "` is, you can see a full list of settings in `" + ctx.getPrefix() + "help settings`");
            return;
        }

        final var settingsFn = this.settingsMap.get(item);
        final boolean shouldSetValue = args.size() >= 3 && "--set".equals(args.get(1));

        settingsFn.accept(ctx, item, shouldSetValue);
    }

    /// <editor-fold desc="Settings overview" defaultstate="collapsed">
    private void showSettingsOverview(CommandContext ctx) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final TextChannel logChan = AirUtils.getLogChannel(settings.getLogChannel(), guild);
        final TextChannel welcomeChannel = AirUtils.getLogChannel(settings.getWelcomeLeaveChannel(), guild);
        final Role autoRole = guild.getRoleById(settings.getAutoroleRole());
        final Role muteRole = guild.getRoleById(settings.getMuteRoleId());

        final EmbedBuilder message = EmbedUtils.embedMessage("Here are the settings from this guild.\n" +
            "**Show join/leave messages:** " + toEmoji(settings.isEnableJoinMessage()) + "\n" +
            "**Swearword filter:** " + toEmoji(settings.isEnableSwearFilter()) + "\n" +
            "**Announce next track:** " + toEmoji(settings.isAnnounceTracks()) + "\n" +
            "**Auto de-hoist:** " + toEmoji(settings.isAutoDeHoist()) + "\n" +
            "**Filter Discord invites:** " + toEmoji(settings.isFilterInvites()) + "\n" +
            "**Spamfilter:** " + toEmoji(settings.isEnableSpamFilter()) + "\n" +
            "**Kick Mode:** " + (settings.getKickState() ? "Kick Members" : "Mute members") + "\n" +

            "**MuteRole:** " + (settings.getMuteRoleId() <= 0 || muteRole == null
            ? "Not Set" : muteRole.getAsMention()) + "\n" +

            "**Join message:** " + StringUtils.abbreviate(settings.getCustomJoinMessage(), 50) + "\n" +
            "**Leave message:** " + StringUtils.abbreviate(settings.getCustomLeaveMessage(), 50) + "\n" +

            "**AutoRole:** " + (settings.getAutoroleRole() <= 0 || autoRole == null
            ? "Not Set" : autoRole.getAsMention()) + "\n" +

            "**Current prefix:** " + settings.getCustomPrefix() + "\n" +
            "**Modlog Channel:** " + (logChan == null ? "Not set" : logChan.getAsMention()) + "\n" +
            "**Welcome/Leave channel:** " + (welcomeChannel == null ? "Not set" : welcomeChannel.getAsMention()) + "\n" +
            "**Embed color code:** " + guild.getHexColor()
        );

        sendEmbed(ctx, message);
    }
    /// </editor-fold>

    /// <editor-fold desc="autoRoleSetting" defaultstate="collapsed">
    private void autoRoleSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            sendMsg(ctx, String.format(
                "Autorole is currently set to %s",
                settings.getAutoroleRole() > 0 ? "<@&" + settings.getAutoroleRole() + '>' : "`None`"
            ));
            return;
        }

        if (shouldDisable(ctx)) {
            sendMsg(ctx, "AutoRole feature has been disabled");
            guild.setSettings(settings.setAutoroleRole(0L));
            return;
        }

        final Role role = fetchRoleWithChecks(ctx);

        if (role == null) {
            return;
        }

        guild.setSettings(settings.setAutoroleRole(role.getIdLong()));

        sendMsg(ctx, "AutoRole has been set to " + role.getAsMention());
    }
    /// </editor-fold>

    /// <editor-fold desc="muteRoleSetting" defaultstate="collapsed">
    private void muteRoleSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            sendMsg(ctx, String.format(
                "Mute role is currently set to %s",
                settings.getMuteRoleId() > 0 ? "<@&" + settings.getMuteRoleId() + '>' : "`None`"
            ));
            return;
        }

        if (shouldDisable(ctx)) {
            sendMsg(ctx, "Mute role feature & spam filter have been disabled");
            //Never clean the role's id so activating the filter wont cause issues.
            //GuildSettingsUtils.updateGuildSettings(guild, settings.setMuteRoleId(0L));
            guild.setSettings(settings.setEnableSpamFilter(false));
            return;
        }

        final Role role = fetchRoleWithChecks(ctx);

        if (role == null) {
            return;
        }

        guild.setSettings(settings.setMuteRoleId(role.getIdLong()));

        sendMsg(ctx, "Mute role has been set to " + role.getAsMention());
    }
    /// </editor-fold>

    /// <editor-fold desc="embedColorSetting" defaultstate="collapsed">
    private void embedColorSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();

        if (!setValue) {
            final String msg = String.format("Current embed color is `%s`", colorToHex(guild.getColor()));

            sendEmbed(ctx, EmbedUtils.embedMessage(msg));
            return;
        }

        final String colorString = this.getSetValue(ctx);
        final Matcher colorMatcher = COLOR_REGEX.matcher(colorString);

        if (!colorMatcher.matches()) {
            sendMsg(ctx, "That color does not look like a valid hex color, hex colors start with a pound sign.\n" +
                "Tip: you can use <http://colorpicker.com/> to generate a hex code.");
            return;
        }

        final int colorInt = colorToInt(colorString);

        guild.setColor(colorInt);

        final String msg = String.format("Embed color has been set to `%s`", colorString);

        sendEmbed(ctx, EmbedUtils.embedMessage(msg));
    }
    /// </editor-fold>

    /// <editor-fold desc="descriptionSetting" defaultstate="collapsed">
    private void descriptionSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            sendMsg(ctx, String.format(
                "You can see the current server description by running `%sguildinfo`",
                settings.getCustomPrefix()
            ));
            return;
        }

        if (shouldDisable(ctx)) {
            sendMsg(ctx, "Description has been reset.");
            guild.setSettings(settings.setServerDesc(null));
            return;
        }

        final String description = StringKt.stripFlags(
            ctx.getArgsRaw(false),
            this
        )
            .replaceFirst(name, "")
            .strip();

        guild.setSettings(settings.setServerDesc(description));

        sendMsg(ctx, "Description has been updated, check `" + ctx.getPrefix() + "guildinfo` to see your description");
    }
    /// </editor-fold>

    /// <editor-fold desc="joinMessageSetting" defaultstate="collapsed">
    private void joinMessageSetting(CommandContext ctx, String name, boolean setValue) {
        if (!setValue) {
            sendMsg(ctx, "The join message can only be previewed on the dashboard <https://dashboard.duncte.bot/>");
            return;
        }

        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();

        final String newJoinMessage = StringKt.stripFlags(
            ctx.getArgsRaw(false),
            this
        )
            .replaceFirst(name, "")
            .replaceAll("\\\\n", "\n")
            .strip();

        guild.setSettings(settings.setCustomJoinMessage(newJoinMessage));
        sendMsg(ctx, "The new join message has been set to `" + newJoinMessage + '`');
    }
    /// </editor-fold>

    /// <editor-fold desc="leaveMessageSetting" defaultstate="collapsed">
    private void leaveMessageSetting(CommandContext ctx, String name, boolean setValue) {
        if (!setValue) {
            sendMsg(ctx, "The leave message can only be previewed on the dashboard <https://dashboard.duncte.bot/>");
            return;
        }

        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();

        final String newLeaveMessage = StringKt.stripFlags(
            ctx.getArgsRaw(false),
            this
        )
            .replaceFirst(name, "")
            .replaceAll("\\\\n", "\n")
            .strip();

        guild.setSettings(settings.setCustomLeaveMessage(newLeaveMessage));
        sendMsg(ctx, "The new leave message has been set to `" + newLeaveMessage + '`');
    }
    /// </editor-fold>

    /// <editor-fold desc="logChannelSetting" defaultstate="collapsed">
    private void logChannelSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            sendMsg(ctx, String.format(
                "The current log channel is %s",
                settings.getLogChannel() > 0 ? "<#" + settings.getLogChannel() + '>' : "`None`"
            ));
            return;
        }

        if (this.shouldDisable(ctx)) {
            guild.setSettings(settings.setLogChannel(0L));
            sendMsg(ctx, "Logging has been turned off");
            return;
        }

        final TextChannel channel = findTextChannel(ctx);

        if (channel == null) {
            sendMsg(ctx, "I could not found a text channel for your query.\n" +
                "Make sure that it's a valid channel that I can speak in");
            return;
        }

        guild.setSettings(settings.setLogChannel(channel.getIdLong()));
        sendMsg(ctx, "The new log channel has been set to " + channel.getAsMention());
    }
    /// </editor-fold>

    /// <editor-fold desc="prefixSetting" defaultstate="collapsed">
    private void prefixSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            sendMsg(ctx, String.format(
                "The current custom prefix on this server is `%s`",
                settings.getCustomPrefix()
            ));
            return;
        }

        final String newPrefix = this.getSetValue(ctx);

        if (newPrefix.length() > 10) {
            sendErrorWithMessage(ctx.getMessage(), "The length of the prefix must not exceed 10 characters");
            return;
        }

        guild.setSettings(settings.setCustomPrefix(newPrefix));
        sendMsg(ctx, "New prefix has been set to `" + newPrefix + '`');
    }
    /// </editor-fold>

    /// <editor-fold desc="rateLimitSetting" defaultstate="collapsed">
    private void rateLimitSetting(CommandContext ctx, String name, boolean setValue) {
        if (!setValue) {
            sendMsg(ctx, "The rate limits can only be previewed on the dashboard <https://dashboard.duncte.bot/>");
            return;
        }

        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final String newRateLimit = this.getSetValue(ctx, "");

        if ("default".equals(newRateLimit) || "reset".equals(newRateLimit)) {
            sendMsg(ctx, "Rate limits have been reset.");
            guild.setSettings(settings.setRatelimits(new long[]{20, 45, 60, 120, 240, 2400}));
            return;
        }

        final long[] rates = ratelimmitChecks(newRateLimit);

        if (rates.length != 6) {
            sendMsg(ctx, "Invalid rate limit settings (example settings are `20|45|60|120|240|2400`)");
            return;
        }

        guild.setSettings(settings.setRatelimits(rates));
        final String steps = Arrays.stream(rates).mapToObj(String::valueOf)
            .collect(Collectors.joining(", ", "", " minutes"));

        sendMsg(ctx, "New rate limit settings have been set to `" + steps + '`');
    }
    /// </editor-fold>

    /// <editor-fold desc="welcomeChannelSetting" defaultstate="collapsed">
    private void welcomeChannelSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            sendMsg(ctx, String.format(
                "The welcome channel is %s",
                settings.getWelcomeLeaveChannel() > 0 ? "<#" + settings.getWelcomeLeaveChannel() + '>' : "`None`"
            ));
            return;
        }

        final TextChannel channel = findTextChannel(ctx);

        if (channel == null) {
            sendMsg(ctx, "I could not found a text channel for your query.\n" +
                "Make sure that it's a valid channel that I can speak in");
            return;
        }

        guild.setSettings(settings.setWelcomeLeaveChannel(channel.getIdLong()));
        sendMsg(ctx, "The new channel for join and leave messages has been set to " + channel.getAsMention());
    }
    /// </editor-fold>

    /// <editor-fold desc="announceTracksSetting" defaultstate="collapsed">
    private void announceTracksSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final boolean shouldAnnounce = !settings.isAnnounceTracks();

        guild.setSettings(settings.setAnnounceTracks(shouldAnnounce));
        sendMsg(ctx, "Announcing the next track has been toggled **"
            + (shouldAnnounce ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="autoDehoistSetting" defaultstate="collapsed">
    private void autoDehoistSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final boolean shouldAutoDeHoist = !settings.isAutoDeHoist();

        guild.setSettings(settings.setAutoDeHoist(shouldAutoDeHoist));
        sendMsg(ctx, "Auto de-hoisting has been toggled **"
            + (shouldAutoDeHoist ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="filterInvitesSetting" defaultstate="collapsed">
    private void filterInvitesSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final boolean shouldFilter = !settings.isFilterInvites();

        guild.setSettings(settings.setFilterInvites(shouldFilter));
        sendMsg(ctx, "Filtering discord invites has been toggled **"
            + (shouldFilter ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="joinMessageStateSetting" defaultstate="collapsed">
    private void joinMessageStateSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final boolean isEnabled = !settings.isEnableJoinMessage();

        guild.setSettings(
            settings.setEnableJoinMessage(isEnabled).setEnableLeaveMessage(isEnabled)
        );
        sendMsg(ctx, "The join and leave messages have been toggled **"
            + (isEnabled ? "on" : "off") + "** (Tip: you can toggle them individually on the dashboard)");
    }
    /// </editor-fold>

    /// <editor-fold desc="kickModeSetting" defaultstate="collapsed">
    private void kickModeSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final boolean kickState = !settings.getKickState();

        guild.setSettings(settings.setKickState(kickState));
        sendMsg(ctx, "Kick-Mode is now set to **"
            + (kickState ? "kick" : "mute") + "** members");
    }
    /// </editor-fold>

    /// <editor-fold desc="spamFilterSetting" defaultstate="collapsed">
    private void spamFilterSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final long muteRoleId = settings.getMuteRoleId();

        if (muteRoleId <= 0) {
            sendMsg(ctx, "**__Please set a spam/mute role first!__** (`" +
                ctx.getPrefix() + "settings muteRole --set <@role>`)");
            return;
        }

        final boolean spamState = !settings.isEnableSpamFilter();

        guild.setSettings(settings.setEnableSpamFilter(spamState));

        final Role muteRole = guild.getRoleById(muteRoleId);
        final String muteRoleString = muteRole == null ?
            "`Deleted`, please update it using `%ssettings muteRole --set <@role>`" :
            muteRole.getAsMention() + ", make sure to change this using `%ssettings muteRole --set <@role>` if that is incorrect";

        final String message = String.format(
            "The spam filter has been toggled **%s**\nThe current mute role is " + muteRoleString,
            spamState ? "on" : "off",
            ctx.getPrefix()
        );

        sendMsg(ctx, message);
    }
    /// </editor-fold>

    /// <editor-fold desc="swearFilterSetting" defaultstate="collapsed">
    private void swearFilterSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final boolean isEnabled = !settings.isEnableSwearFilter();

        guild.setSettings(settings.setEnableSwearFilter(isEnabled));
        sendMsg(ctx, "The swearword filter has been toggled **" +
            (isEnabled ? "on" : "off") +
            "**.\nThe current filter type is set to `" +
            settings.getFilterType().getName() + "`, this can be changed on <https://dashboard.duncte.bot>");
    }
    /// </editor-fold>

    /// <editor-fold desc="inviteLoggingSetting" defaultstate="collapsed">
    private void inviteLoggingSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final boolean isEnabled = !settings.isFilterInvites();

        guild.setSettings(settings.setFilterInvites(isEnabled));
        sendMsg(ctx, "The logging of invites has been toggled **" +
            (isEnabled ? "on" : "off") + "**");

        if (isEnabled) {
            // attempt caching
            ((EventManager) ctx.getJDA().getEventManager())
                .getInviteTracker()
                .attemptInviteCaching(ctx.getJDAGuild());
        }
    }
    /// </editor-fold>

    /// <editor-fold desc="memberLoggingSetting" defaultstate="collapsed">
    private void memberLoggingSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSetting settings = guild.getSettings();
        final long logChannel = settings.getLogChannel();

        if (logChannel < 1 || ctx.getGuild().getTextChannelById(logChannel) == null) {
            sendMsg(ctx, "There currently is no log channel set, please set this first with `" +
                ctx.getPrefix() + "settings logChannel --set #channel`");
            return;
        }

        final boolean isEnabled = !settings.isMemberLogging();

        guild.setSettings(settings.setMemberLogging(isEnabled));
        sendMsg(ctx, "The logging of members joining and leaving has been toggled **" +
            (isEnabled ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="helpers" defaultstate="collapsed">
    private void loadSettingsMap() {
        this.settingsMap.put("autoRole", this::autoRoleSetting);
        this.settingsMap.put("muteRole", this::muteRoleSetting);
        this.settingsMap.put("embedColor", this::embedColorSetting);
        this.settingsMap.put("description", this::descriptionSetting);
        this.settingsMap.put("joinMessage", this::joinMessageSetting);
        this.settingsMap.put("leaveMessage", this::leaveMessageSetting);
        this.settingsMap.put("logChannel", this::logChannelSetting);
        this.settingsMap.put("prefix", this::prefixSetting);
        this.settingsMap.put("rateLimits", this::rateLimitSetting);
        this.settingsMap.put("welcomeChannel", this::welcomeChannelSetting);
        this.settingsMap.put("announceTracks", this::announceTracksSetting);
        this.settingsMap.put("autoDehoist", this::autoDehoistSetting);
        this.settingsMap.put("filterInvites", this::filterInvitesSetting);
        this.settingsMap.put("joinMessageState", this::joinMessageStateSetting);
        this.settingsMap.put("kickMode", this::kickModeSetting);
        this.settingsMap.put("spamFilter", this::spamFilterSetting);
        this.settingsMap.put("swearFilter", this::swearFilterSetting);
        this.settingsMap.put("inviteLogging", this::inviteLoggingSetting);
        this.settingsMap.put("memberLogging", this::memberLoggingSetting);
    }

    @Nullable
    private Role fetchRoleWithChecks(CommandContext ctx) {
        if (doesNotPassRolePermCheck(ctx)) {
            return null;
        }

        return getFoundRoleOrNull(ctx);
    }

    @Nullable
    private Role getFoundRoleOrNull(CommandContext ctx) {
        final List<Role> mentionedRoles = ctx.getMessage().getMentionedRoles();

        final Role foundRole;

        if (mentionedRoles.isEmpty()) {
            final String query = this.getSetValue(ctx);

            foundRole = FinderUtil.findRoles(query, ctx.getGuild())
                .stream()
                .filter((role) -> ctx.getSelfMember().canInteract(role))
                .findFirst()
                .orElse(null);
        } else {
            foundRole = mentionedRoles.get(0);
        }

        if (foundRole == null) {
            sendMsg(ctx, "I'm sorry but I could not find any roles for your input, " +
                "make sure that the target role is below my role.");
            return null;
        }

        if (foundRole.isManaged()) {
            final Role.RoleTags tags = foundRole.getTags();

            if (tags.isBot()) {
                sendMsg(ctx, "I cannot give this role to members because it belongs to <@" + tags.getBotIdLong() + '>');
            } else if (tags.isBoost()) {
                sendMsg(ctx, "I cannot give the boost role to members");
            } else if (tags.isIntegration()) {
                sendMsg(ctx, "I cannot give this role to members because it is managed by an integration (for example twitch subscriber roles)");
            } else {
                sendMsg(ctx, "This role cannot be used, but I don't know why (`unknown managed role`)");
            }

            return null;
        }

        return foundRole;
    }

    private String getSetValue(CommandContext ctx) {
        return this.getSetValue(ctx, " ");
    }

    private String getSetValue(CommandContext ctx, String joiner) {
        return String.join(joiner, ctx.getParsedFlags(this).get("set"));
    }

    @Nullable
    private TextChannel findTextChannel(@Nonnull CommandContext ctx) {
        final List<TextChannel> foundChannels = FinderUtil.findTextChannels(this.getSetValue(ctx), ctx.getGuild());

        if (foundChannels.isEmpty()) {
            return null;
        }

        return foundChannels.stream()
            .filter(TextChannel::canTalk)
            .findFirst()
            .orElse(null);
    }

    private boolean shouldDisable(CommandContext ctx) {
        // This call is safe as the flags are cached
        final String query = this.getSetValue(ctx);

        return List.of(
            "none",
            "disable",
            "disabled",
            "off",
            "remove",
            "removed",
            "none",
            "null",
            "reset"
        ).contains(query);
    }

    private boolean doesNotPassRolePermCheck(CommandContext ctx) {
        if (!ctx.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            sendMsg(ctx, "I need the _Manage Roles_ permission in order for this feature to work.");

            return true;
        }

        final List<Role> selfRoles = ctx.getSelfMember().getRoles();

        if (selfRoles.isEmpty()) {
            sendMsg(ctx, "I need a role above the specified role in order for this feature to work.");

            return true;
        }

        return false;
    }

    /// </editor-fold>
}
