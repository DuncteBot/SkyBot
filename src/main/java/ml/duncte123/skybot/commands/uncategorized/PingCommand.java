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

package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PingCommand extends Command {
    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        if (!ctx.getChannel().canTalk()) {
            return;
        }

        final AtomicLong start = new AtomicLong();

        ctx.getChannel().sendMessage("Pong!!")
            .setCheck(() -> {
                start.set(System.currentTimeMillis());

                return true;
            })
            .queue((it) -> {
                final double restPing = System.currentTimeMillis() - start.get();
                final long messagePing = ctx.getMessage().getCreationTime().until(it.getCreationTime(), ChronoUnit.MILLIS);

                it.editMessageFormat("PONG!\n" +
                        "Rest ping: %sms\n" +
                        "Message ping: %sms\n" +
                        "Websocket ping: %sms\n" +
                        "Average shard ping: %sms",
                    restPing,
                    messagePing,
                    ctx.getJDA().getPing(),
                    ctx.getShardManager().getAveragePing()
                ).queue();
            });
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String help() {
        return "Shows the delay from the bot to the discord servers.";
    }
}
