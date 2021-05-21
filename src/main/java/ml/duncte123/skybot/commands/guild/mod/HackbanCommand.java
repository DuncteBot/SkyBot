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

import io.sentry.Sentry;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.internal.requests.RestActionImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class HackbanCommand extends ModBaseCommand {

    public HackbanCommand() {
        this.requiresArgs = true;
        this.name = "hackban";
        this.help = "Ban a user before they can join your server.";
        this.usage = "<userId...> [-r reason]";
        this.botPermissions = new Permission[]{
            Permission.BAN_MEMBERS,
        };
        this.flags = new Flag[]{
            new Flag(
                'r',
                "reason",
                "Sets the reason for this hackban (default: `Hackban by user#discrim`)"
            )
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final Map<String, List<String>> parsedFlags = ctx.getParsedFlags(this);
        final List<String> args = parsedFlags.get("undefined");
        final List<String> userids = new ArrayList<>();

        final String reason;

        if (parsedFlags.containsKey("r")) {
            reason = ctx.getAuthor() + ": " + String.join(" ", parsedFlags.get("r"));
        } else {
            reason = String.format("Hackban by %#s", ctx.getAuthor());
        }

        for (final String arg : args) {
            String userId = "";

            if (arg.matches("<@\\d{17,20}>")) {
                userId = arg.substring(2, args.get(0).length() - 1);
            } else if (arg.matches(".{2,32}#\\d{4}")) {

                final Optional<User> opt = ctx.getShardManager().getUserCache()
                    .getElementsByName(arg.substring(0, arg.length() - 5), false)
                    .stream()
                    .findFirst();

                if (opt.isPresent()) {
                    userId = opt.get().getId();
                }

            } else if (arg.matches("\\d{17,20}")) {
                userId = arg;
            } else {
                sendMsg(ctx, "id `" + arg + "` does not match anything valid or is not a known user");
                continue;
            }

            if (userId.isBlank()) {
                sendMsg(ctx, "Found empty id, aborting");

                return;
            }

            try {
                final String finalId = userId;

                ctx.getGuild().ban(finalId, 0, reason)
                    .reason(reason)
                    .queue(null, (thr) -> {
                        if (thr instanceof ErrorResponseException err) {
                            sendMsg(ctx, "Could not ban `" + finalId + "`, reason: " + err.getMeaning());
                        } else {
                            RestActionImpl.getDefaultFailure().accept(thr);
                        }
                });
                userids.add(finalId);
            }
            catch (HierarchyException e) {
              sendMsg(ctx, String.format("Could not ban id `%s`", userId));
            } catch (Exception e) {
                Sentry.captureException(e);
                sendMsg(ctx, "ERROR: " + e.getMessage());
                return;
            }
        }

        if (userids.isEmpty()) {
            sendMsg(ctx, "Could not ban any of the user ids provided");
            return;
        }

        sendMsg(
            ctx,
            String.format(
                "Users with ids `%s` are now banned with reason `%s`",
                String.join("`, `", userids),
                reason
            )
        );
    }
}
