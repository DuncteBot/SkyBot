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

package ml.duncte123.skybot.commands.essentials;

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.CommandUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class TestTagCommand extends Command {

    public TestTagCommand() {
        this.category = CommandCategory.UTILS;
    }

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {

        if (ctx.getArgs().isEmpty()) {
            sendMsg(ctx.getEvent(), "Usage: `" + ctx.getPrefix() + getName() + " <JagTag syntax>`");
            return;
        }

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final String input = ctx.getArgsRaw();

        if (input.length() > 1000) {
            sendMsg(event, "Please limit your input to 1000 characters.");
            return;
        }

        final String output = CommandUtils.parseJagTag(ctx, input);

        final String message = new MessageBuilder()
            .append("**Input:**")
            .appendCodeBlock(input, "pascal")
            .append('\n')
            .append("**Output:**\n")
            .append(output)
            .getStringBuilder().toString();

        sendEmbed(event, EmbedUtils.embedMessage(message));

    }

    @NotNull
    @Override
    public String getName() {
        return "testtag";
    }

    @NotNull
    @Override
    public String[] getAliases() {
        return new String[]{"tt"};
    }

    @NotNull
    @Override
    public String help(@NotNull String prefix) {
        return "Test your jagtag format before you save it as custom command etc.\n" +
            "Usage: `" + prefix + getName() + " <JagTag syntax>`";
    }
}
