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

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class ToggleSpamFilterCommand extends SettingsBase {

    public ToggleSpamFilterCommand() {
        this.name = "togglespamfilter";
        this.help = "Toggles whether we should handle your incoming spam";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

        final long muteRoleId = settings.getMuteRoleId();

        if (muteRoleId <= 0) {
            sendMsg(ctx, "**__Please set a spam/mute role first!__**");
            return;
        }

        final boolean spamState = !settings.isEnableSpamFilter();
        guild.setSettings(settings.setEnableSpamFilter(spamState));
        String message = String.format("Spamfilter **%s**!", (spamState ? "activated" : "disabled"));

        final Role r = guild.getRoleById(muteRoleId);
        message += "\nThe spam role is " + ((r == null) ? "deleted. Please update it." : r.getName() + ". Change it if it's outdated.");

        sendMsg(ctx, message);
    }
}
