/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botCommons.messaging.MessageUtils.sendMsg;

public class HackbanCommand extends Command {

    public HackbanCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)) {
            sendMsg(event, "You need the kick members and the ban members permission for this command, please contact your server administrator about this");
            return;
        }

        if (args.size() < 1) {
            sendMsg(event, "Usage is " + PREFIX + getName() + " <userId...>");
            return;
        }

        List<String> messages = new ArrayList<>();

        ctx.getChannel().sendTyping().queue();

        for (String arg0 : args) {
            String id = "";

            if (arg0.matches("<@\\d{17,20}>")) {
                id = arg0.substring(2, args.get(0).length() - 1);
            } else if (arg0.matches(".{2,32}#\\d{4}")) {

                Optional<User> opt = event.getJDA().getUsersByName(arg0.substring(0, arg0.length() - 5), false).stream()
                        .findFirst();

                if (opt.isPresent()) {
                    id = opt.get().getId();
                }

            } else if (arg0.matches("\\d{17,20}")) {
                id = arg0;
            } else {
                messages.add("id `" + arg0 + "` does not match anything valid");
            }

            try {
                event.getGuild().getController().ban(id, 0)
                        .reason(String.format("Hackban by %#s", ctx.getAuthor())).complete(); //Commands are being ran on a separate thread, this is safe
                messages.add("User with id " + id + " has been banned!");
            } catch (Exception e) {
                e.printStackTrace();
                messages.add("ERROR: " + e.getMessage());
            }
        }

        sendMsg(event, String.join("\n", messages));
        messages.clear();
    }

    @Override
    public String help() {
        return "Ban a user before he/she can join your guild.\nUsage: " + PREFIX + getName() + " <userId>";
    }

    @Override
    public String getName() {
        return "hackban";
    }
}
