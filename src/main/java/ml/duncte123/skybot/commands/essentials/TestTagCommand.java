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

package ml.duncte123.skybot.commands.essentials;

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.CustomCommandUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class TestTagCommand extends Command {

    public TestTagCommand() {
        this.category = CommandCategory.NERD_STUFF;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        if (ctx.getArgs().isEmpty()) {
            sendMsg(ctx.getEvent(), "Usage: `" + getName() + " <JagTag syntax>");
            return;
        }

        GuildMessageReceivedEvent event = ctx.getEvent();
        String input = ctx.getArgsRaw();

        if (input.length() > 1000) {
            sendMsg(event, "Pleas limit your input to 1000 characters.");
            return;
        }

        String output = CustomCommandUtils.PARSER.clear()
            .put("user", event.getAuthor())
            .put("channel", event.getChannel())
            .put("guild", event.getGuild())
            .put("args", ctx.getArgsJoined())
            .parse(input);

        String message = new MessageBuilder()
            .append("**Input:**")
            .appendCodeBlock(input, "perl")
            .append('\n')
            .append("**Output:**\n")
            .append(output)
            .getStringBuilder().toString();

        sendEmbed(event, EmbedUtils.embedMessage(message));

    }

    @Override
    public String getName() {
        return "testtag";
    }

    @Override
    public String help() {
        return "Test your jagtag format before you save it as custom command etc.\n" +
            "Usage: `" + PREFIX + getName() + " <JagTag syntax>`";
    }
}
