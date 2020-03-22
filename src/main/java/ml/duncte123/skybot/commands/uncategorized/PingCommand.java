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

package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormat;

public class PingCommand extends Command {

    public PingCommand() {
        this.name = "ping";
        this.help = "Shows the delay from the bot to the discord servers";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (!ctx.getChannel().canTalk()) {
            return;
        }

        ctx.getJDA().getRestPing().queue((restPing) ->
            sendMsgFormat(ctx.getChannel(),
                "PONG!\n" +
                    "Rest ping: %sms\n" +
                    "Message ping: %sms\n" +
                    "Websocket ping: %sms\n" +
                    "Average shard ping: %sms",
                restPing,
                ctx.getMessage().getTimeCreated().until(OffsetDateTime.now(), ChronoUnit.MILLIS),
                ctx.getJDA().getGatewayPing(),
                ctx.getShardManager().getAverageGatewayPing())
        );

    }
}
