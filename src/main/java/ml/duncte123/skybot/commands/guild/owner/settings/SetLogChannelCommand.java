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
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetLogChannelCommand extends SettingsBase {
    @Override
    public void run(@NotNull CommandContext ctx) {
        if (ctx.getArgs().isEmpty()) {
            sendMsg(ctx.getEvent(), "Incorrect usage: `" + Settings.PREFIX + "setLogChannel [text channel]`");
            return;
        }

        if ("null".equalsIgnoreCase(ctx.getArgsJoined()) || "none".equalsIgnoreCase(ctx.getArgsJoined()) || "off".equalsIgnoreCase(ctx.getArgsJoined())) {
            ctx.getGuild().setSettings(ctx.getGuildSettings().setLogChannel(0L));
            sendMsg(ctx.getEvent(), "Logging has been turned off");
            return;
        }

        final TextChannel channel = findTextChannel(ctx);

        if (channel == null) {
            sendMsg(ctx.getEvent(), "I could not found a text channel for your query.\n" +
                "Make sure that it's a valid channel that I can speak in");
            return;
        }

        ctx.getGuild().setSettings(ctx.getGuildSettings().setLogChannel(channel.getIdLong()));
        sendMsg(ctx.getEvent(), "The new log channel has been set to " + channel.getAsMention());
    }

    @Override
    public String getName() {
        return "setlogchannel";
    }

    @Override
    public String help() {
        return "Sets the channel to log messages in\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <text channel>`";
    }
}
