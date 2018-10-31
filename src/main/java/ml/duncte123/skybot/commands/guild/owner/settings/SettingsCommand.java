/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class SettingsCommand extends SettingsBase {
    @Override
    public void run(@NotNull CommandContext ctx) {

        DunctebotGuild guild = ctx.getGuild();
        GuildSettings settings = guild.getSettings();

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
            "**Spamfilter:** " + boolToEmoji(settings.getEnableSpamFilter()) + "\n" +
            "**Kick Mode:** " + (settings.getKickState() ? "Kick Members" : "Mute members") + "\n" +
            "**MuteRole:** " + (settings.getMuteRoleId() <= 0
            ? "Not Set" : guild.getRoleById(settings.getMuteRoleId()).getAsMention()) + "\n" +
            "**Join message:** " + settings.getCustomJoinMessage() + "\n" +
            "**Leave message:** " + settings.getCustomLeaveMessage() + "\n" +
            "**AutoRole:** " + (settings.getAutoroleRole() <= 0
            ? "Not Set" : guild.getRoleById(settings.getAutoroleRole()).getAsMention()) + "\n" +
            "**Current prefix:** " + settings.getCustomPrefix() + "\n" +
            "**Modlog Channel:** " + (logChan != null ? logChan.getAsMention() : "none") + "\n" +
            "**Welcome/Leave channel:** " + (welcomeLeaveChannel != null ? welcomeLeaveChannel.getAsMention() : "none")
        );

        sendEmbed(ctx.getEvent(), message);
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"options"};
    }

    @Override
    public String help() {
        return "Shows the current settings\n" +
            "Usage: `" + PREFIX + getName() + "`";
    }

    private String boolToEmoji(boolean flag) {
        return flag ? "<:check:414777605141561344>" : "<:xmark:414777605250875402>";
    }
}
