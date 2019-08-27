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

import io.sentry.Sentry;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
        this.helpFunction = (invoke, prefix) -> "Performs a cleanup in the channel where the command is run.\n" +
            "To clear an entire channel it's better to use `" + prefix + "purgechannel`";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " [amount] [--keep-pinned] [--bots-only]`";
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
                'p',
                "keep-pinned",
                "If this flag is set the messages that are pinned in the channel will be skipped"
            ),
            new Flag(
                'b',
                "bots-only",
                "If this flag is set only messages that are from bots will be deleted"
            ),
        };
    }

    @Override
    public void run(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();

        int total = 5;

        if (args.size() > 3) {
            sendErrorWithMessage(event.getMessage(), "Usage: " + this.getUsageInstructions(ctx.getInvoke(), ctx.getPrefix()));
            return;
        }

        final var flags = ctx.getParsedFlags(this);
        final boolean keepPinned = flags.containsKey("p");
        final boolean clearBots = flags.containsKey("b");

        // if size == 0 then this will just be skipped
        for (final String arg : args) {
            if (AirUtils.isInt(arg)) {
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
                        "To clear an entire channel it's better to use `" + ctx.getPrefix() + "purgechannel`");
                    return;
                }
            }
        }

        final TextChannel channel = event.getChannel();
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

            CompletableFuture<Message> hack = new CompletableFuture<>();
            sendMsg(event, "Deleting messages, please wait (this might take a while)", hack::complete);

            final List<CompletableFuture<Void>> futures =  channel.purgeMessages(msgList);

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            }
            catch (InterruptedException | ExecutionException e) {
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

            sendMsg(event, "ERROR: " + thr.getMessage() + cause);

            return 0;
        }).whenCompleteAsync((count, thr) -> {
            sendMsgFormatAndDeleteAfter(event, 5, TimeUnit.SECONDS, "Removed %d messages! (this message will auto delete in 5 seconds)", count);

            modLog(String.format("%d messages removed in %s by %s", count, channel, ctx.getAuthor().getAsTag()), ctx.getGuild());
        });
        // End of the annotation
    }

    private void removeMessage(TextChannel channel, CompletableFuture<Message> hack) {
        try {
            Message hacked = hack.get();

            if (hacked != null) {
                channel.deleteMessageById(hacked.getIdLong()).queue();
            }
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
