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
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.CommandContext;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetLeaveMessageCommand extends SettingsBase {
    @Override
    public void run(@NotNull CommandContext ctx) {
        if (ctx.getArgs().isEmpty()) {
            sendMsg(ctx.getEvent(), "Correct usage is `" + Settings.PREFIX + "setleavemessage <new join message>`");
            return;
        }

        String newLeaveMessage = ctx.getArgsRaw().replaceAll("\n", "\\\\n")/*.replaceAll("\n", "\r\n")*/;
        ctx.getGuild().setSettings(ctx.getGuildSettings().setCustomLeaveMessage(newLeaveMessage));

        sendMsg(ctx.getEvent(), "The new leave message has been set to `" + newLeaveMessage + "`");
    }

    @Override
    public String getName() {
        return "setleavemessage";
    }

    @Override
    public String help() {
        return "Sets the message that the bot shows when a member leaves\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <leave message>`";
    }
}
