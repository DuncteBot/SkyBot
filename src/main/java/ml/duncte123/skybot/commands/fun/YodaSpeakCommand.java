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

package ml.duncte123.skybot.commands.fun;

import com.fasterxml.jackson.databind.JsonNode;
import io.sentry.Sentry;
import me.duncte123.weebJava.helpers.QueryBuilder;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class YodaSpeakCommand extends Command {
    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (ctx.getArgs().isEmpty()) {
            sendMsg(event, "Correct usage: `" + ctx.getPrefix() + getName() + " <A sentence.>`");
            return;
        }

        try {
            final QueryBuilder builder = new QueryBuilder()
                .append("yoda")
                .append("sentence", ctx.getArgsDisplay());
            final JsonNode response = ctx.getApis().executeDefaultGetRequest(builder.build(), false);

            logger.debug("Yoda response: " + response);

            if (!response.get("success").asBoolean()) {
                sendMsg(event, "Could not connect to yoda service, try again in a few hours");
                return;
            }

            sendMsg(event, "<:yoda:578198258351079438> " + response.get("data").asText());
        }
        catch (Exception e) {
            Sentry.capture(e);
            sendMsg(event, "Could not connect to yoda service, try again in a few hours");
        }
    }

    @NotNull
    @Override
    public String help(@NotNull String prefix) {
        return "Convert your sentences into yoda speak.\n" +
            "Usage: `" + prefix + getName() + " <A sentence.>`";
    }

    @NotNull
    @Override
    public String getName() {
        return "yoda";
    }

    @NotNull
    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }
}
