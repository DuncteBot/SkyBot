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

package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.SinceSkybot;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import javax.annotation.Nonnull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.duncte123.botcommons.messaging.MessageUtils.*;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

@Author(nickname = "Sanduhr32", author = "Maurice R S")
public class CleanupCommand extends ModBaseCommand {

    public CleanupCommand() {
        this.perms = new Permission[]{Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY};
    }

    @Override
    public void run(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();

        int total = 5;
        boolean keepPinned = false;
        boolean clearBots = false;

        if (args.size() > 3) {
            sendErrorWithMessage(event.getMessage(), "You provided more than three arguments.");
            return;
        }

        // if size == 0 then this will just be skipped
        for (final String arg : args) {
            if (arg.equalsIgnoreCase("keep-pinned")) {
                keepPinned = true;
            } else if (arg.equalsIgnoreCase("bots-only")) {
                clearBots = true;
            } else if (AirUtils.isInt(arg)) {
                try {
                    total = Integer.parseInt(args.get(0));
                }
                catch (NumberFormatException e) {
                    sendError(event.getMessage());
                    sendMsg(event, "Error: Amount to clear is not a valid number");
                    return;
                }
                if (total < 1 || total > 1000) {
                    sendMsgAndDeleteAfter(event, 5, TimeUnit.SECONDS, "Error: count must be minimal 2 and maximal 1000\n" +
                        "To clear an entire channel it's better to use `" + Settings.PREFIX + "purgechannel`");
                    return;
                }
            }
        }

        @SinceSkybot(version = "3.78.2") final boolean keepPinnedFinal = keepPinned;
        final boolean clearBotsFinal = clearBots;
        final TextChannel channel = event.getChannel();
        // Start of the annotation
        channel.getIterableHistory().takeAsync(total).thenApplyAsync((msgs) -> {
            Stream<Message> msgStream = msgs.stream();

            if (keepPinnedFinal) {
                msgStream = msgStream.filter(msg -> !msg.isPinned());
            }
            if (clearBotsFinal) {
                msgStream = msgStream.filter(msg -> msg.getAuthor().isBot());
            }

            final List<Message> msgList = msgStream.collect(Collectors.toList());

            channel.purgeMessages(msgList);

            return msgList.size();
        }).exceptionally((thr) -> {
            String cause = "";

            if (thr.getCause() != null) {
                cause = " caused by: " + thr.getCause().getMessage();
            }

            sendMsg(event, "ERROR: " + thr.getMessage() + cause);

            return 0;
        }).whenCompleteAsync((count, thr) -> {
            sendMsgFormatAndDeleteAfter(event, 10, TimeUnit.SECONDS, "Removed %d messages!", count);

            modLog(String.format("%d messages removed in %s", count, channel), ctx.getGuild());
        });
        // End of the annotation
    }

    @Override
    public String help() {
        return "Performs a cleanup in the channel where the command is run.\n" +
            "To clear an entire channel it's better to use `" + Settings.PREFIX + "purgechannel`\n" +
            "Usage: `" + Settings.PREFIX + getName() + "[ammount] [keep-pinned] [bots-only]`";
    }

    @Override
    public String getName() {
        return "cleanup";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"clear", "purge", "wipe"};
    }
}
