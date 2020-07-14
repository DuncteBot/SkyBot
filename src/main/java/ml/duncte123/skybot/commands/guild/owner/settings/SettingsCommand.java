/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.commands.guild.owner.settings;

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.extensions.StringKt;
import ml.duncte123.skybot.objects.TriConsumer;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.*;
import static ml.duncte123.skybot.extensions.BooleanKt.toEmoji;
import static ml.duncte123.skybot.utils.AirUtils.colorToHex;
import static ml.duncte123.skybot.utils.AirUtils.colorToInt;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SettingsCommand extends SettingsBase {
    public static final Pattern COLOR_REGEX = Pattern.compile("#[a-zA-Z0-9]{6}");

    // db!setting prefix
    // db!setting prefix --set !

    private final Map<String, TriConsumer<CommandContext, String, Boolean>> settingsMap = new HashMap<>();

    // TODO: make clear help of all the items

    public SettingsCommand() {
        // Override category here to make sure that we can hide all the other settings commands
        this.category = CommandCategory.ADMINISTRATION;
        this.name = "settings";
        this.aliases = new String[]{
            "setting",
            "options",
        };
        this.help = "Shows the current settings for this server";
        this.usage = "[item] [--set value]";
        // TODO: add extra data for the user to see
        this.flags = new Flag[] {
            new Flag(
                "set",
                "Sets the value for this item"
            ),
        };

        this.loadSettingsMap();
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
            // TODO
            return;
        }

        final var settingsFn = this.settingsMap.get(item);
        final boolean shouldSetValue = args.size() >= 3 && "--set".equals(args.get(1));

        settingsFn.accept(ctx, item, shouldSetValue);
    }

    /// <editor-fold desc="Settings overview" defaultstate="collapsed">
    private void showSettingsOverview(CommandContext ctx) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();
        final TextChannel logChan = AirUtils.getLogChannel(settings.getLogChannel(), guild);
        final TextChannel welcomeLeaveChannel = AirUtils.getLogChannel(settings.getWelcomeLeaveChannel(), guild);
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

            "**Join message:** " + settings.getCustomJoinMessage() + "\n" +
            "**Leave message:** " + settings.getCustomLeaveMessage() + "\n" +

            "**AutoRole:** " + (settings.getAutoroleRole() <= 0 || autoRole == null
            ? "Not Set" : autoRole.getAsMention()) + "\n" +

            "**Current prefix:** " + settings.getCustomPrefix() + "\n" +
            "**Modlog Channel:** " + (logChan == null ? "Not set" : logChan.getAsMention()) + "\n" +
            "**Welcome/Leave channel:** " + (welcomeLeaveChannel == null ? "Not set" : welcomeLeaveChannel.getAsMention()) + "\n" +
            "**Embed color code:** " + guild.getHexColor()
        );

        sendEmbed(ctx, message);
    }
    /// </editor-fold>

    private void loadSettingsMap() {
        this.settingsMap.put("autoRole", this::autoRoleSetting);
        // TODO: add spamrole as well?
        this.settingsMap.put("muteRole", this::muteRoleSetting);
//        this.settingsMap.put("spamrole", this::muteRoleSetting);
        this.settingsMap.put("embedColor", this::embedColorSetting);
        // TODO: add color as well?
//        this.settingsMap.put("color", this::dummyMethod);
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
        this.settingsMap.put("swearFilter", this::dummyMethod);
    }

    @Nullable
    private Role fetchRoleWithChecks(CommandContext ctx) {
        if (doesNotPassRolePermCheck(ctx)) {
            return null;
        }

        return getFoundRoleOrNull(ctx);
    }

    /// <editor-fold desc="autoRoleSetting" defaultstate="collapsed">
    private void autoRoleSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

        if (!setValue) {
            sendMsgFormat(
                ctx,
                "Autorole is currently set to %s",
                settings.getAutoroleRole() > 0 ? "<@&" + settings.getAutoroleRole() + '>' : "`None`"
            );
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
        final GuildSettings settings = guild.getSettings();

        if (!setValue) {
            sendMsgFormat(
                ctx,
                "Mute role is currently set to %s",
                settings.getMuteRoleId() > 0 ? "<@&" + settings.getMuteRoleId() + '>' : "`None`"
            );
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
        final GuildSettings settings = guild.getSettings();

        if (!setValue) {
            sendMsgFormat(ctx, "You can see the current server description by running `%sguildinfo`", settings.getCustomPrefix());
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
            sendMsg(ctx, "The join message can only be previewed on the dashboard <https://dashboard.dunctebot.com/>");
            return;
        }

        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

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
            sendMsg(ctx, "The leave message can only be previewed on the dashboard <https://dashboard.dunctebot.com/>");
            return;
        }

        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

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
        final GuildSettings settings = guild.getSettings();

        if (!setValue) {
            sendMsgFormat(
                ctx,
                "The current log channel is %s",
                settings.getLogChannel() > 0 ? "<#" + settings.getLogChannel() + '>' : "`None`"
            );
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
        final GuildSettings settings = guild.getSettings();

        if (!setValue) {
            sendMsgFormat(ctx, "The current custom prefix on this server is `%s`", settings.getCustomPrefix());
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
            sendMsg(ctx, "The rate limits can only be previewed on the dashboard <https://dashboard.dunctebot.com/>");
            return;
        }

        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();
        final String newRateLimit = this.getSetValue(ctx, "");

        if ("default".equals(newRateLimit) || "reset".equals(newRateLimit)) {
            sendMsg(ctx, "Rate limits have been reset.");
            guild.setSettings(settings.setRatelimits(new long[]{20, 45, 60, 120, 240, 2400}));
            return;
        }

        final long[] rates = GuildSettingsUtils.ratelimmitChecks(newRateLimit);

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
        final GuildSettings settings = guild.getSettings();

        if (!setValue) {
            sendMsgFormat(
                ctx,
                "The welcome channel is %s",
                settings.getWelcomeLeaveChannel() > 0 ? "<#" + settings.getWelcomeLeaveChannel() + '>' : "`None`"
            );
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
        final GuildSettings settings = guild.getSettings();
        final boolean shouldAnnounceTracks = !settings.isAnnounceTracks();

        guild.setSettings(settings.setAnnounceTracks(shouldAnnounceTracks));
        sendMsg(ctx, "Announcing the next track has been toggled **"
            + (shouldAnnounceTracks ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="autoDehoistSetting" defaultstate="collapsed">
    private void autoDehoistSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();
        final boolean shouldAutoDeHoist = !settings.isAutoDeHoist();

        guild.setSettings(settings.setAutoDeHoist(shouldAutoDeHoist));
        sendMsg(ctx, "Auto de-hoisting has been toggled **"
            + (shouldAutoDeHoist ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="filterInvitesSetting" defaultstate="collapsed">
    private void filterInvitesSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();
        final boolean shouldFilterInvites = !settings.isFilterInvites();

        guild.setSettings(settings.setFilterInvites(shouldFilterInvites));
        sendMsg(ctx, "Filtering discord invites has been toggled **"
            + (shouldFilterInvites ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="filterInvitesSetting" defaultstate="collapsed">
    private void joinMessageStateSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();
        final boolean isEnabled = !settings.isEnableJoinMessage();

        guild.setSettings(settings.setEnableJoinMessage(isEnabled));
        sendMsg(ctx, "The join and leave messages have been toggled **"
            + (isEnabled ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="kickModeSetting" defaultstate="collapsed">
    private void kickModeSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();
        final boolean kickState = !settings.getKickState();

        guild.setSettings(settings.setKickState(kickState));
        sendMsg(ctx, "Kick-Mode is set to **"
            + (kickState ? "kick" : "mute") + "** members");
    }
    /// </editor-fold>

    /// <editor-fold desc="spamFilterSetting" defaultstate="collapsed">
    private void spamFilterSetting(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();
        // TODO: test
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

    /// <editor-fold desc="dummyMethod" defaultstate="uncollapsed">
    private void dummyMethod(CommandContext ctx, String name, boolean setValue) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

        sendMsg(ctx, name + " is not yet implemented");
    }
    /// </editor-fold>
}
