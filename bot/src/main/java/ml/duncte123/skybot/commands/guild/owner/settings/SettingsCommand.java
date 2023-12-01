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
import com.google.common.base.CaseFormat;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.duncte123.botcommons.StringUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.EventManager;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.SlashSupport;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
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
public class SettingsCommand extends SlashSupport {
    public static final Pattern COLOR_REGEX = Pattern.compile("#[a-zA-Z0-9]{6}");

    private final Map<String, SettingData> settingsMap = new ConcurrentHashMap<>();

    private interface MessageHandler {
        void accept(String message);
        void accept(EmbedBuilder message);
    }

    @FunctionalInterface
    private interface SettingsConsumer {
        void accept(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler);
    }

    /**
     * @param handler
     *     Guild, Item name, setValue, value, messageHandler
     */
    private record SettingData(String help, SettingsConsumer handler, boolean hasSetter) {
        SettingData(String help, SettingsConsumer handler) {
            this(help, handler, true);
        }
    }

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
            • `autoRole`: Sets the role given to users on join, set value is a role
            • `muteRole`: Sets the role given to users when they get muted, set value is a role
            • `embedColor`: Sets the color of the embeds DuncteBot sends, set value is a valid hex color
            • `description`: Sets the description in the server info command, set value is a piece of text
            • `joinMessage`: Sets the message being send to `welcomeChannel` when a user joins, set value is the message
            • `leaveMessage`: Sets the message being send to `welcomeChannel` when a user leaves, set value is the message
            • `logChannel`: Sets the channel where moderation actions are logged, set value is a text channel
            • `prefix`: Sets a custom prefix for the bot, set value is your desired prefix
            • `rateLimits`: Sets the cooldown in minutes for un-muting your spammer of choice, set value is in the format `1|2|3|4|5|6` or `default`
            • `welcomeChannel`: Sets the channel where the join and leave messages are send, set value is a text channel
            • `announceTracks`: Toggles the announcing of the next playing track on or off, this item has no set value
            • `autoDehoist`: Toggles the enabled state of auto de-hoisting, this item has no set value
            • `filterInvites`: Toggles if the bot should filter discord invites for messages, this item has no set value
            • `joinMessageState`: Toggles the join and leave messages on or off, this item has no set value
            • `kickMode`: Toggles the kick mode for spammers between muting and kicking, this item has no set value
            • `spamFilter`: Toggles the spam filter on or off, this item has no set value
            • `swearFilter`: Toggles the swear filter on or off, this item has no set value
            • `inviteLogging`: Toggles the logging of invite usage on or off, this item has no set value
            • `memberLogging`: Toggles the logging of members joining and leaving on or off, this item has no set value
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
        final String value = shouldSetValue ? getSetValue(ctx) : "";
        final var messageHandler = new MessageHandler() {
            @Override
            public void accept(String message) {
                sendMsg(ctx, message);
            }

            @Override
            public void accept(EmbedBuilder message) {
                sendEmbed(ctx, message);
            }
        };

        sendMsg(ctx, "This command will soon be a slash command only, sorry for the inconvenience");
        settingsFn.handler.accept(
            ctx.getGuild(),
            ctx::getMessage,
            item,
            shouldSetValue,
            value,
            messageHandler
        );
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
    private void autoRoleSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            messageHandler.accept(
                String.format(
                    "Autorole is currently set to %s",
                    settings.getAutoroleRole() > 0 ? "<@&" + settings.getAutoroleRole() + '>' : "`None`"
                )
            );
            return;
        }

        if (shouldDisable(value)) {
            messageHandler.accept("AutoRole feature has been disabled");
            guild.setSettings(settings.setAutoroleRole(0L));
            return;
        }

        final Role role = fetchRoleWithChecks(guild, message.get(), value, messageHandler);

        if (role == null) {
            return;
        }

        guild.setSettings(settings.setAutoroleRole(role.getIdLong()));

        messageHandler.accept("AutoRole has been set to " + role.getAsMention());
    }
    /// </editor-fold>

    /// <editor-fold desc="muteRoleSetting" defaultstate="collapsed">
    private void muteRoleSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            messageHandler.accept(String.format(
                "Mute role is currently set to %s",
                settings.getMuteRoleId() > 0 ? "<@&" + settings.getMuteRoleId() + '>' : "`None`"
            ));
            return;
        }

        if (shouldDisable(value)) {
            messageHandler.accept("Mute role feature & spam filter have been disabled");
            //Never clean the role's id so activating the filter wont cause issues.
            //GuildSettingsUtils.updateGuildSettings(guild, settings.setMuteRoleId(0L));
            guild.setSettings(settings.setEnableSpamFilter(false));
            return;
        }

        final Role role = fetchRoleWithChecks(guild, message.get(), value, messageHandler);

        if (role == null) {
            return;
        }

        guild.setSettings(settings.setMuteRoleId(role.getIdLong()));

        messageHandler.accept("Mute role has been set to " + role.getAsMention());
    }
    /// </editor-fold>

    /// <editor-fold desc="embedColorSetting" defaultstate="collapsed">
    private void embedColorSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        if (!setValue) {
            final String msg = String.format("Current embed color is `%s`", colorToHex(guild.getSettings().getEmbedColor()));

            messageHandler.accept(EmbedUtils.embedMessage(msg));
            return;
        }

        final Matcher colorMatcher = COLOR_REGEX.matcher(value);

        if (!colorMatcher.matches()) {
            messageHandler.accept("That color does not look like a valid hex color, hex colors start with a pound sign.\n" +
                "Tip: you can use <https://colorpicker.com/> to generate a hex code.");
            return;
        }

        final int colorInt = colorToInt(value);

        guild.setSettings(guild.getSettings().setEmbedColor(colorInt));

        final String msg = String.format("Embed color has been set to `%s`", value);

        messageHandler.accept(EmbedUtils.embedMessage(msg));
    }
    /// </editor-fold>

    /// <editor-fold desc="descriptionSetting" defaultstate="collapsed">
    private void descriptionSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            messageHandler.accept(String.format(
                "You can see the current server description by running `%sguildinfo`",
                settings.getCustomPrefix()
            ));
            return;
        }

        if (shouldDisable(value)) {
            messageHandler.accept("Description has been reset.");
            guild.setSettings(settings.setServerDesc(null));
            return;
        }

        guild.setSettings(settings.setServerDesc(
            value
                .replaceAll("\\\\n", "\n")
                .strip()
        ));

        messageHandler.accept("Description has been updated, check `" + settings.getCustomPrefix() + "guildinfo` to see your description\nPlease note that this feature will be removed soon in favour of server descriptions.");
    }
    /// </editor-fold>

    /// <editor-fold desc="joinMessageSetting" defaultstate="collapsed">
    private void joinMessageSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        if (!setValue) {
            messageHandler.accept("The join message can only be previewed on the dashboard <https://dashboard.duncte.bot/>");
            return;
        }

        final GuildSetting settings = guild.getSettings();

        final String newJoinMessage = value
            .replaceAll("\\\\n", "\n")
            .strip();

        guild.setSettings(settings.setCustomJoinMessage(newJoinMessage));
        messageHandler.accept("The new join message has been set to `" + newJoinMessage + '`');
    }
    /// </editor-fold>

    /// <editor-fold desc="leaveMessageSetting" defaultstate="collapsed">
    private void leaveMessageSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        if (!setValue) {
            messageHandler.accept("The leave message can only be previewed on the dashboard <https://dashboard.duncte.bot/>");
            return;
        }

        final GuildSetting settings = guild.getSettings();

        final String newLeaveMessage = value
            .replaceAll("\\\\n", "\n")
            .strip();

        guild.setSettings(settings.setCustomLeaveMessage(newLeaveMessage));
        messageHandler.accept("The new leave message has been set to `" + newLeaveMessage + '`');
    }
    /// </editor-fold>

    /// <editor-fold desc="logChannelSetting" defaultstate="collapsed">
    private void logChannelSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            messageHandler.accept(String.format(
                "The current log channel is %s",
                settings.getLogChannel() > 0 ? "<#" + settings.getLogChannel() + '>' : "`None`"
            ));
            return;
        }

        if (shouldDisable(value)) {
            guild.setSettings(settings.setLogChannel(0L));
            messageHandler.accept("Logging has been turned off");
            return;
        }

        final TextChannel channel = findTextChannel(guild, value);

        if (channel == null) {
            messageHandler.accept("I could not found a text channel for your query.\n" +
                "Make sure that it's a valid channel that I can speak in");
            return;
        }

        guild.setSettings(settings.setLogChannel(channel.getIdLong()));
        messageHandler.accept("The new log channel has been set to " + channel.getAsMention());
    }
    /// </editor-fold>

    /// <editor-fold desc="prefixSetting" defaultstate="collapsed">
    private void prefixSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            messageHandler.accept(String.format(
                "The current custom prefix on this server is `%s`",
                settings.getCustomPrefix()
            ));
            return;
        }

        if (value.length() > 10) {
            messageHandler.accept(getErrorReaction() + " The length of the prefix must not exceed 10 characters");
            return;
        }

        guild.setSettings(settings.setCustomPrefix(value));
        messageHandler.accept("New prefix has been set to `" + value + '`');
    }
    /// </editor-fold>

    /// <editor-fold desc="rateLimitSetting" defaultstate="collapsed">
    private void rateLimitSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        if (!setValue) {
            messageHandler.accept("The rate limits can only be previewed on the dashboard <https://dashboard.duncte.bot/>");
            return;
        }

        final GuildSetting settings = guild.getSettings();

        if ("default".equals(value) || "reset".equals(value)) {
            messageHandler.accept("Rate limits have been reset.");
            guild.setSettings(settings.setRatelimits(new long[]{20, 45, 60, 120, 240, 2400}));
            return;
        }

        final long[] rates = ratelimmitChecks(value);

        if (rates.length != 6) {
            messageHandler.accept("Invalid rate limit settings (example settings are `20|45|60|120|240|2400`)");
            return;
        }

        guild.setSettings(settings.setRatelimits(rates));
        final String steps = Arrays.stream(rates).mapToObj(String::valueOf)
            .collect(Collectors.joining(", ", "", " minutes"));

        messageHandler.accept("New rate limit settings have been set to `" + steps + '`');
    }
    /// </editor-fold>

    /// <editor-fold desc="welcomeChannelSetting" defaultstate="collapsed">
    private void welcomeChannelSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();

        if (!setValue) {
            messageHandler.accept(String.format(
                "The welcome channel is %s",
                settings.getWelcomeLeaveChannel() > 0 ? "<#" + settings.getWelcomeLeaveChannel() + '>' : "`None`"
            ));
            return;
        }

        final TextChannel channel = findTextChannel(guild, value);

        if (channel == null) {
            messageHandler.accept("I could not found a text channel for your query.\n" +
                "Make sure that it's a valid channel that I can speak in");
            return;
        }

        guild.setSettings(settings.setWelcomeLeaveChannel(channel.getIdLong()));
        messageHandler.accept("The new channel for join and leave messages has been set to " + channel.getAsMention());
    }
    /// </editor-fold>

    /// <editor-fold desc="announceTracksSetting" defaultstate="collapsed">
    private void announceTracksSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();
        final boolean shouldAnnounce = !settings.isAnnounceTracks();

        guild.setSettings(settings.setAnnounceTracks(shouldAnnounce));
        messageHandler.accept("Announcing the next track has been toggled **"
            + (shouldAnnounce ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="autoDehoistSetting" defaultstate="collapsed">
    private void autoDehoistSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();
        final boolean shouldAutoDeHoist = !settings.isAutoDeHoist();

        guild.setSettings(settings.setAutoDeHoist(shouldAutoDeHoist));
        messageHandler.accept("Auto de-hoisting has been toggled **"
            + (shouldAutoDeHoist ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="filterInvitesSetting" defaultstate="collapsed">
    private void filterInvitesSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();
        final boolean shouldFilter = !settings.isFilterInvites();

        guild.setSettings(settings.setFilterInvites(shouldFilter));
        messageHandler.accept("Filtering discord invites has been toggled **"
            + (shouldFilter ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="joinMessageStateSetting" defaultstate="collapsed">
    private void joinMessageStateSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();
        final boolean isEnabled = !settings.isEnableJoinMessage();

        guild.setSettings(
            settings.setEnableJoinMessage(isEnabled).setEnableLeaveMessage(isEnabled)
        );
        messageHandler.accept("The join and leave messages have been toggled **"
            + (isEnabled ? "on" : "off") + "** (Tip: you can toggle them individually on the dashboard)");
    }
    /// </editor-fold>

    /// <editor-fold desc="kickModeSetting" defaultstate="collapsed">
    private void kickModeSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();
        final boolean kickState = !settings.getKickState();

        guild.setSettings(settings.setKickState(kickState));
        messageHandler.accept("Kick-Mode is now set to **"
            + (kickState ? "kick" : "mute") + "** members");
    }
    /// </editor-fold>

    /// <editor-fold desc="spamFilterSetting" defaultstate="collapsed">
    private void spamFilterSetting(DunctebotGuild guild, Supplier<Message> msgSub, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();
        final long muteRoleId = settings.getMuteRoleId();
        final String prefix = settings.getCustomPrefix();

        if (muteRoleId <= 0) {
            messageHandler.accept("**__Please set a spam/mute role first!__** (`" +
                prefix + "settings muteRole --set <@role>`)");
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
            prefix
        );

        messageHandler.accept(message);
    }
    /// </editor-fold>

    /// <editor-fold desc="swearFilterSetting" defaultstate="collapsed">
    private void swearFilterSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();
        final boolean isEnabled = !settings.isEnableSwearFilter();

        guild.setSettings(settings.setEnableSwearFilter(isEnabled));
        messageHandler.accept("The swearword filter has been toggled **" +
            (isEnabled ? "on" : "off") +
            "**.\nThe current filter type is set to `" +
            settings.getFilterType().getName() + "`, this can be changed on <https://dashboard.duncte.bot>");
    }
    /// </editor-fold>

    /// <editor-fold desc="inviteLoggingSetting" defaultstate="collapsed">
    private void inviteLoggingSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();
        final boolean isEnabled = !settings.isFilterInvites();

        guild.setSettings(settings.setFilterInvites(isEnabled));
        messageHandler.accept("The logging of invites has been toggled **" +
            (isEnabled ? "on" : "off") + "**");

        if (isEnabled) {
            // attempt caching
            ((EventManager) guild.getJDA().getEventManager())
                .getInviteTracker()
                .attemptInviteCaching(guild);
        }
    }
    /// </editor-fold>

    /// <editor-fold desc="memberLoggingSetting" defaultstate="collapsed">
    private void memberLoggingSetting(DunctebotGuild guild, Supplier<Message> message, String name, boolean setValue, String value, MessageHandler messageHandler) {
        final GuildSetting settings = guild.getSettings();
        final long logChannel = settings.getLogChannel();

        if (logChannel < 1 || guild.getTextChannelById(logChannel) == null) {
            messageHandler.accept("There currently is no log channel set, please set this first with `" +
                settings.getCustomPrefix() + "settings logChannel --set #channel`");
            return;
        }

        final boolean isEnabled = !settings.isMemberLogging();

        guild.setSettings(settings.setMemberLogging(isEnabled));
        messageHandler.accept("The logging of members joining and leaving has been toggled **" +
            (isEnabled ? "on" : "off") + "**");
    }
    /// </editor-fold>

    /// <editor-fold desc="helpers" defaultstate="collapsed">
    private void loadSettingsMap() {
        this.settingsMap.put(
            "autoRole",
            new SettingData(
                "Sets the role given to users on join",
                this::autoRoleSetting
            )
        );
        this.settingsMap.put(
            "muteRole",
            new SettingData(
                "Sets the role given to users when they get muted",
                this::muteRoleSetting
            )
        );
        this.settingsMap.put(
            "embedColor",
            new SettingData(
                "Sets the color of the embeds DuncteBot sends",
                this::embedColorSetting
            )
        );
        this.settingsMap.put(
            "description",
            new SettingData(
                "(Deprecated) Sets the description in the server info command",
                this::descriptionSetting
            )
        );
        this.settingsMap.put(
            "joinMessage",
            new SettingData(
                "Sets the message being send to `welcomeChannel` when a user joins",
                this::joinMessageSetting
            )
        );
        this.settingsMap.put(
            "leaveMessage",
            new SettingData(
                "Sets the message being send to `welcomeChannel` when a user leaves",
                this::leaveMessageSetting
            )
        );
        this.settingsMap.put(
            "logChannel",
            new SettingData(
                "Sets the channel where moderation actions are logged",
                this::logChannelSetting
            )
        );
        this.settingsMap.put(
            "prefix",
            new SettingData(
                "Sets a custom prefix for the bot",
                this::prefixSetting
            )
        );
        this.settingsMap.put(
            "rateLimits",
            new SettingData(
                "Sets the cooldown in minutes for un-muting your spammer of choice",
                this::rateLimitSetting
            )
        );
        this.settingsMap.put(
            "welcomeChannel",
            new SettingData(
                "Sets the channel where the join and leave messages are send",
                this::welcomeChannelSetting
            )
        );
        this.settingsMap.put(
            "announceTracks",
            new SettingData(
                "Toggles the announcing of the next playing track on or off",
                this::announceTracksSetting,
                false
            )
        );
        this.settingsMap.put(
            "autoDehoist",
            new SettingData(
                "Toggles the enabled state of auto de-hoisting",
                this::autoDehoistSetting,
                false
            )
        );
        this.settingsMap.put(
            "filterInvites",
            new SettingData(
                "Toggles if the bot should filter discord invites for messages",
                this::filterInvitesSetting,
                false
            )
        );
        this.settingsMap.put(
            "joinMessageState",
            new SettingData(
                "Toggles the join and leave messages on or off",
                this::joinMessageStateSetting,
                false
            )
        );
        this.settingsMap.put(
            "kickMode",
            new SettingData(
                "Toggles the kick mode for spammers between muting and kicking",
                this::kickModeSetting,
                false
            )
        );
        this.settingsMap.put(
            "spamFilter",
            new SettingData(
                "Toggles the spam filter on or off",
                this::spamFilterSetting,
                false
            )
        );
        this.settingsMap.put(
            "swearFilter",
            new SettingData(
                "Toggles the swear filter on or off",
                this::swearFilterSetting,
                false
            )
        );
        this.settingsMap.put(
            "inviteLogging",
            new SettingData(
                "Toggles the logging of invite usage on or off",
                this::inviteLoggingSetting,
                false
            )
        );
        this.settingsMap.put(
            "memberLogging",
            new SettingData(
                "Toggles the logging of members joining and leaving on or off",
                this::memberLoggingSetting,
                false
            )
        );
    }

    @Nullable
    private Role fetchRoleWithChecks(Guild guild, Message message, String value, MessageHandler messageHandler) {
        if (doesNotPassRolePermCheck(guild.getSelfMember(), messageHandler)) {
            return null;
        }

        return getFoundRoleOrNull(guild, message, value, messageHandler);
    }

    @Nullable
    private Role getFoundRoleOrNull(Guild guild, Message message, String query, MessageHandler messageHandler) {
        final List<Role> mentionedRoles = message.getMentions().getRoles();

        final Role foundRole;

        if (mentionedRoles.isEmpty()) {
            foundRole = FinderUtil.findRoles(query, guild)
                .stream()
                .filter((role) -> guild.getSelfMember().canInteract(role))
                .findFirst()
                .orElse(null);
        } else {
            foundRole = mentionedRoles.get(0);
        }

        if (foundRole == null) {
            messageHandler.accept("I'm sorry but I could not find any roles for your input, " +
                "make sure that the target role is below my role.");
            return null;
        }

        if (foundRole.isManaged()) {
            final Role.RoleTags tags = foundRole.getTags();

            if (tags.isBot()) {
                messageHandler.accept("I cannot give this role to members because it belongs to <@" + tags.getBotIdLong() + '>');
            } else if (tags.isBoost()) {
                messageHandler.accept("I cannot give the boost role to members");
            } else if (tags.isIntegration()) {
                messageHandler.accept("I cannot give this role to members because it is managed by an integration (for example twitch subscriber roles)");
            } else {
                messageHandler.accept("This role cannot be used, but I don't know why (`unknown managed role`)");
            }

            return null;
        }

        return foundRole;
    }

    private String getSetValue(CommandContext ctx) {
        return String.join(" ", ctx.getParsedFlags(this).get("set"));
    }

    @Nullable
    private TextChannel findTextChannel(@Nonnull Guild guild, String value) {
        final List<TextChannel> foundChannels = FinderUtil.findTextChannels(value, guild);

        if (foundChannels.isEmpty()) {
            return null;
        }

        return foundChannels.stream()
            .filter(TextChannel::canTalk)
            .findFirst()
            .orElse(null);
    }

    private boolean shouldDisable(String query) {
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

    private boolean doesNotPassRolePermCheck(Member selfMember, MessageHandler messageHandler) {
        if (!selfMember.hasPermission(Permission.MANAGE_ROLES)) {
            messageHandler.accept("I need the _Manage Roles_ permission in order for this feature to work.");

            return true;
        }

        final List<Role> selfRoles = selfMember.getRoles();

        if (selfRoles.isEmpty()) {
            messageHandler.accept("I need a role above the specified role in order for this feature to work.");

            return true;
        }

        return false;
    }

    /// </editor-fold>

    @Override
    protected void configureSlashSupport(@NotNull SlashCommandData data) {
        final List<SubcommandData> subCmds = this.settingsMap.entrySet().stream().map((e) -> {
                final var key = e.getKey();
                final var value = e.getValue();
                final SubcommandData subCmd = new SubcommandData(
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, key),
                    value.help
                );

                if (value.hasSetter) {
                    subCmd.addOption(
                        OptionType.STRING,
                        "set",
                        "Set the value for this command",
                        false
                    );
                }

                return subCmd;
            })
            .toList();

        data.addSubcommands(subCmds);
    }

    @Override
    public void handleEvent(@NotNull SlashCommandInteractionEvent event, @NotNull Variables variables) {
        final Guild jdaGuild = event.getGuild();

        if (jdaGuild == null) {
            event.reply("How are you not in a guild?").setEphemeral(true).queue();
            return;
        }

        final String subcommandName = event.getSubcommandName();

        if (subcommandName == null) {
            event.reply("Missing sub command? WAT").setEphemeral(true).queue();
            return;
        }

        final String key = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, subcommandName);

        if (!this.settingsMap.containsKey(key)) {
            event.reply("Invalid subcommand? WAT").setEphemeral(true).queue();
            return;
        }

        final OptionMapping option = event.getOption("set");
        final boolean shouldSet = option != null;
        final var guild = new DunctebotGuild(jdaGuild, variables);

        final var messageHandler = new MessageHandler() {
            @Override
            public void accept(String message) {
                event.reply(message).queue();
            }

            @Override
            public void accept(EmbedBuilder message) {
                event.replyEmbeds(
                    message
                        .setColor(EmbedUtils.getColorOrDefault(guild.getIdLong()))
                        .build()
                ).queue();
            }
        };
        this.settingsMap.get(key).handler.accept(
            guild,
            () -> event.getHook().retrieveOriginal().complete(),
            key,
            shouldSet,
            shouldSet ? option.getAsString() : "",
            messageHandler
        );
    }
}
