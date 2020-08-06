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
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageConfigDefaults.DELETE_MESSAGE_AFTER_SECONDS;
import static me.duncte123.botcommons.messaging.MessageUtils.*;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;
import static net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE;

public class PurgeUserCommand extends ModBaseCommand {
    private static final int DEL_COUNT = 200;

    public PurgeUserCommand() {
        this.requiresArgs = true;
        this.name = "purgeuser";
        this.help = "Purges the last " + DEL_COUNT + " messages of a user";
        this.usage = "<@user>";
        this.userPermissions = new Permission[]{
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_HISTORY,
        };
        this.botPermissions = new Permission[]{
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_HISTORY,
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<Member> mentionedMembers = ctx.getMentionedArg(0);

        if (mentionedMembers.isEmpty()) {
            sendMsg(ctx, "I could not find any members with that name");
            return;
        }

        final Member targetMember = mentionedMembers.get(0);
        final User targetUser = targetMember.getUser();
        final TextChannel channel = ctx.getChannel();
        final Message message = ctx.getMessage();

        channel.getIterableHistory()
            .takeAsync(DEL_COUNT)
            .thenApplyAsync(
                (msgs) -> msgs.stream()
                    .filter((msg) -> msg.getAuthor().equals(targetUser))
                    .collect(Collectors.toList())
            )
            .thenApplyAsync((msgs) -> {
                final List<CompletableFuture<Void>> futures = channel.purgeMessages(msgs);

                sendSuccess(message);

                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
                }
                catch (InterruptedException | ExecutionException e) {
                    Sentry.capture(e);

                    return 0;
                }

                return msgs.size();
            })
            .exceptionally((thr) -> {
                String cause = "";

                if (thr.getCause() != null) {
                    cause = " caused by: " + thr.getCause().getMessage();
                }

                sendMsg(ctx, "ERROR: " + thr.getMessage() + cause);

                sendError(message);

                return 0;
            })
            .whenCompleteAsync((count, thr) -> {
                sendMsg(DELETE_MESSAGE_AFTER_SECONDS.apply(5L)
                    .setChannel(ctx.getChannel())
                    .setMessageFormat("Removed %d messages! (this message will auto delete in 5 seconds)", count)
                    .build());

                channel.deleteMessageById(message.getIdLong()).queue(null, ignore(UNKNOWN_MESSAGE));

                modLog(String.format("%d messages by %#s removed in %s by %#s", count, targetUser, channel, ctx.getAuthor()), ctx.getGuild());
            });
    }
}
