/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.funcmds;

import com.fasterxml.jackson.databind.JsonNode;
import io.sentry.Sentry;
import me.duncte123.botcommons.messaging.MessageConfig;
import me.duncte123.weebJava.helpers.QueryBuilder;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class YodaSpeakCommand extends Command {

    public YodaSpeakCommand() {
        this.requiresArgs = true;
        this.category = CommandCategory.FUN;
        this.name = "yoda";
        this.help = "Convert your input to how Yoda speaks";
        this.usage = "<your sentence>";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        try {
            final QueryBuilder builder = new QueryBuilder()
                .append("yoda")
                .append("sentence", ctx.getArgsDisplay());
            final JsonNode response = ctx.getApis().executeDefaultGetRequest(builder.build(), false);

            LOGGER.debug("Yoda response: " + response);

            if (!response.get("success").asBoolean()) {
                sendMsg(ctx, "Could not connect to yoda service, try again in a few hours");
                return;
            }

            final String yoda = ctx.getRandom().nextInt(2) == 1 ? "<:yoda:578198258351079438> " : "<:BABY_YODA:670269491736870972> ";

            sendMsg(
                MessageConfig.Builder.fromCtx(ctx)
                    .replyTo(ctx.getMessage())
                    .setMessage(yoda + response.get("data").asText())
            );
        }
        catch (Exception e) {
            Sentry.captureException(e);
            sendMsg(ctx, "Could not connect to yoda service, try again in a few hours");
        }
    }
}
