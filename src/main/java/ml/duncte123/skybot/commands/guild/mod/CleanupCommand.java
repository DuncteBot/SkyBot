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

package ml.duncte123.skybot.commands.guild.mod;

import io.sentry.Sentry;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.duncte123.botcommons.messaging.MessageUtils.*;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

@Author(nickname = "Sanduhr32", author = "Maurice R S")
public class CleanupCommand extends ModBaseCommand {

    public CleanupCommand() {
        this.name = "cleanup";
        this.aliases = new String[]{
            "clear",
            "purge",
            "wipe",
        };
        this.help = "Performs a purge in the channel where the command is run.\n" +
            "To clear an entire channel it's better to use `{prefix}purgechannel`";
        this.usage = "[amount] [--keep-pinned] [--bots-only]";
        this.userPermissions = new Permission[]{
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_HISTORY,
        };
        this.botPermissions = new Permission[]{
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_HISTORY,
        };
        this.flags = new Flag[]{
            new Flag(
                "keep-pinned",
                "If this flag is set the messages that are pinned in the channel will be skipped"
            ),
            new Flag(
                "bots-only",
                "If this flag is set only messages that are from bots will be deleted"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        int total = 5;

        if (args.size() > 3) {
            sendErrorWithMessage(ctx.getMessage(), "Usage: " + this.getUsageInstructions(ctx.getInvoke(), ctx.getPrefix()));
            return;
        }

        final var flags = ctx.getParsedFlags(this);
        final boolean keepPinned = flags.containsKey("keep-pinned");
        final boolean clearBots = flags.containsKey("bots-only");

        // if size == 0 then this will just be skipped
        for (final String arg : args) {
            if (AirUtils.isInt(arg)) {
                try {
                    total = Integer.parseInt(args.get(0));
                }
                catch (NumberFormatException e) {
                    sendError(ctx.getMessage());
                    sendMsg(ctx, "Error: Amount to clear is not a valid number");
                    return;
                }
                if (total < 1 || total > 1000) {
                    sendMsgAndDeleteAfter(ctx.getEvent(), 5, TimeUnit.SECONDS, "Error: count must be minimal 2 and maximal 1000\n" +
                        "To clear an entire channel it's better to use `" + ctx.getPrefix() + "purgechannel`");
                    return;
                }
            }
        }

        final TextChannel channel = ctx.getChannel();
        // Start of the annotation
        channel.getIterableHistory().takeAsync(total).thenApplyAsync((msgs) -> {
            Stream<Message> msgStream = msgs.stream();

            if (keepPinned) {
                msgStream = msgStream.filter((msg) -> !msg.isPinned());
            }
            if (clearBots) {
                msgStream = msgStream.filter((msg) -> msg.getAuthor().isBot());
            }

            final List<Message> msgList = msgStream
                //TODO: Still needed?
//                .filter((msg) -> msg.getCreationTime().isBefore(OffsetDateTime.now().plus(2, ChronoUnit.WEEKS)))
                .collect(Collectors.toList());

            final CompletableFuture<Message> hack = new CompletableFuture<>();
            sendMsg(ctx, "Deleting messages, please wait this might take a while (message will be deleted once complete)", hack::complete);

            try {
                final List<CompletableFuture<Void>> futures = channel.purgeMessages(msgList);

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            } catch (ErrorResponseException e) {
                if (e.getErrorResponse() == ErrorResponse.UNKNOWN_CHANNEL) {
                    modLog(String.format("Failed to delete messages in %s, channel was deleted during progress", channel), ctx.getGuild());
                    return -100;
                }

                Sentry.capture(e);
            } catch (InterruptedException | ExecutionException e) {
                Sentry.capture(e);
            } finally {
                removeMessage(channel, hack);
            }

            return msgList.size();
        }).exceptionally((thr) -> {
            String cause = "";

            if (thr.getCause() != null) {
                cause = " caused by: " + thr.getCause().getMessage();
            }

            sendMsg(ctx, "ERROR: " + thr.getMessage() + cause);

            return -100;
        }).whenCompleteAsync((count, thr) -> {
            if (count == -100) {
                return;
            }

            sendMsgFormatAndDeleteAfter(ctx.getEvent(), 5, TimeUnit.SECONDS, "Removed %d messages! (this message will auto delete in 5 seconds)", count);

            modLog(String.format("%d messages removed in %s by %s", count, channel, ctx.getAuthor().getAsTag()), ctx.getGuild());
        });
        // End of the annotation
    }

    private void removeMessage(TextChannel channel, CompletableFuture<Message> hack) {
        try {
            final Message hacked = hack.get();

            if (hacked != null) {
                channel.deleteMessageById(hacked.getIdLong()).queue();
            }
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
