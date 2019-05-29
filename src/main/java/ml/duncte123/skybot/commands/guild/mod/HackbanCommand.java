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
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class HackbanCommand extends ModBaseCommand {

    @Override
    public void run(@Nonnull CommandContext ctx) {
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
                sendMsg(event, "id `" + arg0 + "` does not match anything valid");
                return;
            }

            try {
                event.getGuild().getController().ban(id, 0)
                    .reason(String.format("Hackban by %#s", ctx.getAuthor())).queue();
                messages.add(id);
            }
            catch (Exception e) {
                logger.error("Hackban Error", e);
                sendMsg(event, "ERROR: " + e.getMessage());
                return;
            }
        }

        sendMsg(event, String.format("Users with ids `%s` are now banned", String.join("`, `", messages)));
    }

    @Override
    public String help(String prefix) {
        return "Ban a user before he/she can join your guild.\n" +
            "Usage: `" + prefix + getName() + " <userId...>`";
    }

    @Override
    public String getName() {
        return "hackban";
    }
}
