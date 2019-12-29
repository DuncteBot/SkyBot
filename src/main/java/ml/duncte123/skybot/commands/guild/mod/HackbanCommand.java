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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.internal.requests.RestActionImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormat;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class HackbanCommand extends ModBaseCommand {

    public HackbanCommand() {
        this.requiresArgs = true;
        this.name = "hackban";
        this.helpFunction = (prefix, invoke) -> "Ban a user before they can join your server.";
        this.usageInstructions = (prefix, invoke) -> '`' + prefix + invoke + " <userId...>`";
        this.botPermissions = new Permission[]{
            Permission.BAN_MEMBERS,
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final List<String> messages = new ArrayList<>();

        for (final String arg0 : args) {
            String id = "";

            if (arg0.matches("<@\\d{17,20}>")) {
                id = arg0.substring(2, args.get(0).length() - 1);
            } else if (arg0.matches(".{2,32}#\\d{4}")) {

                final Optional<User> opt = ctx.getShardManager().getUserCache()
                    .getElementsByName(arg0.substring(0, arg0.length() - 5), false)
                    .stream()
                    .findFirst();

                if (opt.isPresent()) {
                    id = opt.get().getId();
                }

            } else if (arg0.matches("\\d{17,20}")) {
                id = arg0;
            } else {
                sendMsg(event, "id `" + arg0 + "` does not match anything valid or is not a known user");
                continue;
            }

            if (id.isBlank()) {
                sendMsg(event, "Found empty id, aborting");

                return;
            }

            try {
                final String finalId = id;
                final String reason = String.format("Hackban by %#s", ctx.getAuthor());
                event.getGuild().ban(finalId, 0, reason)
                    .reason(reason)
                    .queue(null, (thr) -> {
                        if (thr instanceof ErrorResponseException) {
                            sendMsg(event, "Could not ban `" + finalId + "`, reason: " + ((ErrorResponseException) thr).getMeaning());
                        } else {
                            RestActionImpl.getDefaultFailure().accept(thr);
                        }
                });
                messages.add(finalId);
            }
            catch (HierarchyException e) {
              sendMsgFormat(ctx, "Could not ban id `%s`", id);
            } catch (Exception e) {
                Sentry.capture(e);
                sendMsg(event, "ERROR: " + e.getMessage());
                return;
            }
        }

        sendMsg(event, String.format("Users with ids `%s` are now banned", String.join("`, `", messages)));
    }
}
