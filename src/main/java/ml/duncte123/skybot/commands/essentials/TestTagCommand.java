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

package ml.duncte123.skybot.commands.essentials;

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.CommandUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class TestTagCommand extends Command {

    public TestTagCommand() {
        this.category = CommandCategory.UTILS;
        this.name = "testtag";
        this.aliases = new String[]{
            "tt",
        };
        this.helpFunction = (prefix, invoke) -> "Test your jagtag format before you save it as custom command etc.";
        this.usageInstructions = (prefix, invoke) -> '`' + prefix + invoke + " <JagTag syntax>`";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {

        if (ctx.getArgs().isEmpty()) {
            this.sendUsageInstructions(ctx);
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
}
