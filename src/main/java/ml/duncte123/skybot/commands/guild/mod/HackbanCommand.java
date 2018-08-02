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
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class HackbanCommand extends Command {

    public HackbanCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)) {
            MessageUtils.sendMsg(event, "You need the kick members and the ban members permission for this command, please contact your server administrator about this");
            return;
        }

        if (args.size() < 1) {
            MessageUtils.sendMsg(event, "Usage is " + PREFIX + getName() + " <userId>");
            return;
        }

        String[] arg = {""};

        if (args.get(0).matches("<@\\d{17,20}>"))
            arg[0] = args.get(0).substring(2, args.get(0).length() - 1);
        else if (args.get(0).matches(".{2,32}#\\d{4}")) {

            event.getJDA().getUsersByName(args.get(0).substring(0, args.get(0).length() - 5), false).stream()
                    .findFirst().ifPresent(user -> arg[0] = user.getId());
        }

        try {
            event.getGuild().getController().ban(arg[0], 0).queue((v) ->
                    MessageUtils.sendMsg(event, "User has been banned!"));
        } catch (Exception e) {
            e.printStackTrace();
            MessageUtils.sendMsg(event, "ERROR: " + e.getMessage());
        }
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
