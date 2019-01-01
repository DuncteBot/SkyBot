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
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendErrorWithMessage;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetDescriptionCommand extends SettingsBase {
    @Override
    public void run(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

        if (args.isEmpty()) {
            sendErrorWithMessage(ctx.getMessage(), "Incorrect usage\n" +
                "Correct usage : `" + Settings.PREFIX + getName() + " <description>`");
            return;
        }

        if ("null".equalsIgnoreCase(args.get(0)) || "disable".equalsIgnoreCase(args.get(0))) {
            guild.setSettings(settings.setServerDesc(null));
            sendMsg(event, "Description has been reset.");
            return;
        }

        final String description = ctx.getArgsRaw().replaceAll("\n", "\\\\n");
        guild.setSettings(settings.setServerDesc(description));

        sendMsg(event, "Description has been updated, check `" + Settings.PREFIX + "guildinfo` to see your description");
    }

    @Override
    public String getName() {
        return "setdescription";
    }

    @Override
    public String help() {
        return "Set a custom description in " + Settings.PREFIX + "guildinfo\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <description>`";
    }
}
