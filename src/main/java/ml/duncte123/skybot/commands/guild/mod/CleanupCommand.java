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

package ml.duncte123.skybot.commands.guild.mod;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.sentry.Sentry;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.RestFuture;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.duncte123.botcommons.messaging.MessageConfigDefaults.DELETE_MESSAGE_AFTER_SECONDS;
import static me.duncte123.botcommons.messaging.MessageUtils.*;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;
import static net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore;

public class CleanupCommand extends ModBaseCommand {
    private final TLongObjectMap<CompletableFuture<Void>> futureMap = new TLongObjectHashMap<>();

    public CleanupCommand() {
        this.name = "cleanup";
        this.aliases = new String[]{
            "clear",
            "purge",
            "wipe",
        };
        this.help = "Performs a purge in the channel where the command is run.\n" +
            "To clear an entire channel it's better to use `{prefix}purgechannel`";
        this.usage = "[amount/cancel] [--keep-pinned] [--bots-only]";
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
        final long channelId = ctx.getChannel().getIdLong();
        final List<String> args = ctx.getArgs();
        final boolean isRunning = this.futureMap.containsKey(channelId);

        if (!args.isEmpty() && "cancel".equals(args.get(0))) {
            if (isRunning) {
                this.futureMap.get(channelId).cancel(true);
            } else {
                sendMsg(ctx, "There is no purge running in this channel");
            }

            return;
        }

        if (isRunning) {
            sendMsg(ctx,
                "This channel is already being cleaned, you can cancel the current purge by running `" +
                    ctx.getPrefix() + ctx.getInvoke() + " cancel`"
            );

            return;
        }

        if (args.size() > 3) {
            sendErrorWithMessage(ctx.getMessage(), "Usage: " + this.getUsageInstructions(ctx.getPrefix(), ctx.getInvoke()));
            return;
        }

        final int total = getTotal(args, ctx);

        // if the total is -1 it means that the user got notified for something being wrong
        if (total == -1) {
            return;
        }

        final TextChannel channel = ctx.getChannel();
        final var flags = ctx.getParsedFlags(this);
        final boolean keepPinned = flags.containsKey("keep-pinned");
        final boolean clearBots = flags.containsKey("bots-only");
        // Start of the annotation
        channel.getIterableHistory()
            .takeAsync(total)
            .thenApplyAsync((msgs) -> handleMessages(msgs, keepPinned, clearBots, ctx))
            .exceptionally((thr) -> handleException(thr, ctx))
            .whenCompleteAsync((count, thr) -> handleComplete(count, ctx));
        // End of the annotation
    }

    private int getTotal(List<String> args, CommandContext ctx) {
        int total = 5;

        // if size == 0 then this will just be skipped
        for (final String arg : args) {
            if (AirUtils.isInt(arg)) {
                try {
                    total = Integer.parseInt(args.get(0));
                }
                catch (NumberFormatException e) {
                    sendError(ctx.getMessage());
                    sendMsg(ctx, "Error: Amount to clear is not a valid number");

                    return -1;
                }
                if (total < 1 || total > 1000) {
                    sendMsg(DELETE_MESSAGE_AFTER_SECONDS.apply(5L)
                        .setChannel(ctx.getChannel())
                        .setMessage("Error: count must be minimal 2 and maximal 1000\n" +
                            "To clear an entire channel it's better to use `" + ctx.getPrefix() + "purgechannel`")
                        .build());

                    return -1;
                }
            }
        }

        return total;
    }

    private int handleMessages(List<Message> msgs, boolean keepPinned, boolean clearBots, CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        Stream<Message> msgStream = msgs.stream();

        if (keepPinned) {
            msgStream = msgStream.filter((msg) -> !msg.isPinned());
        }

        if (clearBots) {
            msgStream = msgStream.filter((msg) -> msg.getAuthor().isBot());
        }

        final List<Message> msgList = msgStream.collect(Collectors.toList());
        final CompletableFuture<Message> hack = new CompletableFuture<>();

        sendMsg(MessageConfig.Builder.fromCtx(ctx)
            .setMessage("Deleting messages, this will take a long time (this message will auto delete when it's done)")
            .setSuccessAction(hack::complete)
            .build());

        try {
            final CompletableFuture<?>[] futures = channel.purgeMessages(msgList)
                .stream()
                .peek((f) -> f.handle(this::checkException))
                .map(this::hackTimeout)
                .toArray(CompletableFuture[]::new);

            final CompletableFuture<Void> futureStore = CompletableFuture.allOf(futures);

            this.futureMap.put(channel.getIdLong(), futureStore);

            futureStore.get();
        }
        catch (ErrorResponseException e) {
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_CHANNEL) {
                modLog(String.format("Failed to delete messages in %s, channel was deleted during progress", channel), ctx.getGuild());
                return -100;
            }

            Sentry.captureException(e);
        }
        catch (CancellationException e) {
            sendSuccess(ctx.getMessage());
            sendMsg(DELETE_MESSAGE_AFTER_SECONDS.apply(5L)
                .setChannel(ctx.getChannel())
                .setMessage("Cancelled successfully.")
                .build());
        }
        catch (InterruptedException | ExecutionException e) {
            Sentry.captureException(e);
        }
        finally {
            removeMessage(channel, hack);
            this.futureMap.remove(channel.getIdLong());
        }

        return msgList.size();
    }

    private <T> T checkException(T value, Throwable thr) {
        if (thr instanceof final ErrorResponseException exception && exception.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
            // Ignore unknown messages
            LOGGER.debug("Recovering from unknown message");
            return null;
        }

        return value;
    }

    private int handleException(Throwable thr, CommandContext ctx) {
        String cause = "";

        if (thr.getCause() != null) {
            cause = " caused by: " + thr.getCause().getMessage();
        }

        sendMsg(ctx, "ERROR: " + thr.getMessage() + cause);

        return -100;
    }

    private void handleComplete(int count, CommandContext ctx) {
        if (count == -100) {
            return;
        }

        sendMsg(DELETE_MESSAGE_AFTER_SECONDS.apply(5L)
            .setChannel(ctx.getChannel())
            .setMessageFormat("Removed %d messages! (this message will auto delete in 5 seconds)", count)
            .setFailureAction(ignore(ErrorResponse.UNKNOWN_MESSAGE))
            .build());

        modLog(String.format(
            "%d messages removed in %s by %#s",
            count,
            ctx.getChannel(),
            ctx.getAuthor()
        ), ctx.getGuild());
    }

    /**
     * This method sets the timeout on a RestAction to -1 to make it not time out
     *
     * @param future
     *     the RestFuture to change the timeout of
     *
     * @return the future with a different timeout
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private CompletableFuture<Void> hackTimeout(CompletableFuture<Void> future) {
        if (!(future instanceof RestFuture)) {
            // Ignore stuff that is not a rest future
            return future;
        }

        final Class<? extends RestFuture> futureClass = ((RestFuture<Void>) future).getClass();

        try {
            final Field requestField = futureClass.getDeclaredField("request");

            requestField.setAccessible(true);

            final Request<Void> request = (Request<Void>) requestField.get(future);
            final Class<? extends Request> requestClass = request.getClass();

            final Field deadlineField = requestClass.getDeclaredField("deadline");

            deadlineField.setAccessible(true);

            // Set the deadline to -1 so this rest action cannot time out
            deadlineField.set(request, -1L);
        }
        catch (NoSuchFieldException | IllegalAccessException ignored) {
            // Ignore this stuff
        }

        return future;
    }

    private void removeMessage(CommandContext ctx, TextChannel channel, CompletableFuture<Message> hack) {
        try {
            final Message hacked = hack.get();

            if (hacked != null) {
                channel.deleteMessageById(hacked.getIdLong())
                    .queue(
                        null,
                        ignore(ErrorResponse.UNKNOWN_MESSAGE)
                            .andThen((e) -> {
                                if (e instanceof ErrorResponseException ex && ex.getErrorResponse() == ErrorResponse.UNKNOWN_CHANNEL) {
                                    modLog(
                                        "Failed to clean own message, text channel was deleted during message delete progress!",
                                        ctx.getGuild()
                                    );
                                }
                            })
                    );
            }
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
