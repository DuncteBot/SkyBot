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

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
abstract class SettingsBase extends Command {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        if (!ctx.getMember().hasPermission(Permission.MANAGE_SERVER) && !isDev(ctx.getAuthor())) {
            sendMsg(ctx.getEvent(), "You need the \"Manage Server\" permission to use this command");
            return;
        }

        run(ctx);
    }

    public abstract void run(@NotNull CommandContext ctx);

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MOD_ADMIN;
    }

    @Nullable
    protected TextChannel findTextChannel(@NotNull CommandContext ctx) {
        List<TextChannel> foundChannels = ctx.getMessage().getMentionedChannels();

        if(foundChannels.isEmpty()) {
            foundChannels.add(
                AirUtils.getLogChannel(ctx.getArgsRaw(), ctx.getGuild())
            );
        }

        if (foundChannels.isEmpty()) {
            return null;
        }

        return foundChannels.stream()
            .filter(TextChannel::canTalk).findFirst().orElse(null);
    }
}
