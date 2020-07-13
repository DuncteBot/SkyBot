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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        sendEmbed(ctx.getEvent(), message);
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
        this.settingsMap.put("joinMessage", this::joinMessage);
        this.settingsMap.put("leaveMessage", this::dummyMethod);
        this.settingsMap.put("logChannel", this::dummyMethod);
        this.settingsMap.put("prefix", this::dummyMethod);
        this.settingsMap.put("rateLimits", this::dummyMethod);
        this.settingsMap.put("welcomeChannel", this::dummyMethod);
        this.settingsMap.put("announceTracks", this::dummyMethod);
        this.settingsMap.put("autoDehoist", this::dummyMethod);
        this.settingsMap.put("filterInvites", this::dummyMethod);
        this.settingsMap.put("enableWelcomeMessage", this::dummyMethod);
        this.settingsMap.put("kickMode", this::dummyMethod);
        this.settingsMap.put("spamFilter", this::dummyMethod);
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

    /// <editor-fold desc="descriptionSetting" defaultstate="uncollapsed">
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


        // TODO: test
        System.out.println(description);

        guild.setSettings(settings.setServerDesc(description));

        sendMsg(ctx, "Description has been updated, check `" + ctx.getPrefix() + "guildinfo` to see your description");
    }
    /// </editor-fold>

    /// <editor-fold desc="joinMessage" defaultstate="uncollapsed">
    private void joinMessage(CommandContext ctx, String name, boolean setValue) {
        if (!setValue) {
            sendMsg(ctx, name + " can only be previewed on the dashboard <https://dashboard.dunctebot.com/>");
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

        // TODO: test

        guild.setSettings(settings.setCustomJoinMessage(newJoinMessage));
        sendMsg(ctx, "The new join message has been set to `" + newJoinMessage + '`');
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
