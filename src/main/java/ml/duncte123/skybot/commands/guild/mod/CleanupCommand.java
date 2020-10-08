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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.sentry.Sentry;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.Author;
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

@Author(nickname = "Sanduhr32", author = "Maurice R S")
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
                    return;
                }
                if (total < 1 || total > 1000) {
                    sendMsg(DELETE_MESSAGE_AFTER_SECONDS.apply(5L)
                        .setChannel(ctx.getChannel())
                        .setMessage("Error: count must be minimal 2 and maximal 1000\n" +
                            "To clear an entire channel it's better to use `" + ctx.getPrefix() + "purgechannel`")
                        .build());
                    return;
                }
            }
        }

        final TextChannel channel = ctx.getChannel();
        final var flags = ctx.getParsedFlags(this);
        final boolean keepPinned = flags.containsKey("keep-pinned");
        final boolean clearBots = flags.containsKey("bots-only");
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
            sendMsg(MessageConfig.Builder.fromCtx(ctx)
                .setMessage("Deleting messages, please wait this might take a long time (this message will be deleted once complete)")
                .setSuccessAction(hack::complete)
                .build());

            try {
                final CompletableFuture<?>[] futures = channel.purgeMessages(msgList)
                    .stream()
                    .map(this::hackTimeout)
                    .toArray(CompletableFuture[]::new);

                final CompletableFuture<Void> futureStore = CompletableFuture.allOf(futures);

                futureMap.put(channel.getIdLong(), futureStore);

                futureStore.get();
            }
            catch (ErrorResponseException e) {
                if (e.getErrorResponse() == ErrorResponse.UNKNOWN_CHANNEL) {
                    modLog(String.format("Failed to delete messages in %s, channel was deleted during progress", channel), ctx.getGuild());
                    return -100;
                }

                Sentry.capture(e);
            }
            catch (CancellationException e) {
                sendSuccess(ctx.getMessage());
                sendMsg(DELETE_MESSAGE_AFTER_SECONDS.apply(5L)
                    .setChannel(ctx.getChannel())
                    .setMessage("Cancelled successfully.")
                    .build());
            }
            catch (InterruptedException | ExecutionException e) {
                Sentry.capture(e);
            }
            finally {
                removeMessage(channel, hack);
                futureMap.remove(channel.getIdLong());
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

            sendMsg(DELETE_MESSAGE_AFTER_SECONDS.apply(5L)
                .setChannel(ctx.getChannel())
                .setMessageFormat("Removed %d messages! (this message will auto delete in 5 seconds)", count)
                .build());

            modLog(String.format("%d messages removed in %s by %s", count, channel, ctx.getAuthor().getAsTag()), ctx.getGuild());
        });
        // End of the annotation
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
