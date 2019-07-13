/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static ml.duncte123.skybot.extensions.BooleanKt.toEmoji;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SettingsCommand extends SettingsBase {
    @Override
    public void run(@Nonnull CommandContext ctx) {
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

            "**MuteRole:** " + (settings.getMuteRoleId() <= 0
            ? "Not Set" : (muteRole == null ? "Not set" : muteRole.getAsMention())) + "\n" +

            "**Join message:** " + settings.getCustomJoinMessage() + "\n" +
            "**Leave message:** " + settings.getCustomLeaveMessage() + "\n" +

            "**AutoRole:** " + (settings.getAutoroleRole() <= 0
            ? "Not Set" : (autoRole == null ? "Not Set" : autoRole.getAsMention())) + "\n" +

            "**Current prefix:** " + settings.getCustomPrefix() + "\n" +
            "**Modlog Channel:** " + (logChan != null ? logChan.getAsMention() : "none") + "\n" +
            "**Welcome/Leave channel:** " + (welcomeLeaveChannel != null ? welcomeLeaveChannel.getAsMention() : "none") + "\n" +
            "**Embed color code:** " + guild.getHexColor()
        );

        sendEmbed(ctx.getEvent(), message);
    }

    @NotNull
    @Override
    public String getName() {
        return "settings";
    }

    @NotNull
    @Override
    public String[] getAliases() {
        return new String[]{"options"};
    }

    @NotNull
    @Override
    public String help(@NotNull String prefix) {
        return "Shows the current settings\n" +
            "Usage: `" + prefix + getName() + "`";
    }
}
