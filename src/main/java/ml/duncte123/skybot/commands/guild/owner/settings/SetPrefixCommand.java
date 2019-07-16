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

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetPrefixCommand extends SettingsBase {
    @Override
    public void run(@Nonnull CommandContext ctx) {
        if (ctx.getArgs().isEmpty()) {
            sendMsg(ctx.getEvent(), "Correct usage is `" + ctx.getPrefix() + "setPrefix <new prefix>`");
            return;
        }

        final String newPrefix = ctx.getArgsJoined();
        ctx.getGuild().setSettings(ctx.getGuildSettings().setCustomPrefix(newPrefix));
        sendMsg(ctx.getEvent(), "New prefix has been set to `" + newPrefix + '`');
    }

    @NotNull
    @Override
    public String getName() {
        return "setprefix";
    }

    @NotNull
    @Override
    public String help(@NotNull String prefix) {
        return "Sets the new prefix\n" +
            "Usage: `" + prefix + getName() + " <prefix>`";
    }
}
