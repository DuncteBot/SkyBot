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
import ml.duncte123.skybot.objects.TriConsumer;
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

import static me.duncte123.botcommons.messaging.MessageUtils.*;
import static ml.duncte123.skybot.extensions.BooleanKt.toEmoji;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SettingsCommand extends SettingsBase {

    // db!setting prefix
    // db!setting prefix --set !

    private final Map<String, TriConsumer<CommandContext, String, Boolean>> settingsMap = new HashMap<>();

    public SettingsCommand() {
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

    private void loadSettingsMap() {
        this.settingsMap.put("autoRole", this::setAutoRole);
        this.settingsMap.put("muteRole", this::dummyMethod);
        this.settingsMap.put("embedColor", this::dummyMethod);
        this.settingsMap.put("color", this::dummyMethod);
        this.settingsMap.put("description", this::dummyMethod);
        this.settingsMap.put("joinMessage", this::dummyMethod);
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

    private void setAutoRole(CommandContext ctx, String name, boolean setValue) {
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

    private void dummyMethod(CommandContext ctx, String name, boolean setValue) {
        //
    }
}
