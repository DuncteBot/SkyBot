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
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class KickCommand extends Command {

    public KickCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        Permission[] perms = {
                Permission.KICK_MEMBERS,
                Permission.BAN_MEMBERS
        };

        if (!event.getMember().hasPermission(perms)) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        if (event.getMessage().getMentionedUsers().size() < 1) {
            sendMsg(event, "Usage is " + Settings.prefix + getName() + " <@user> [Reason]");
            return;
        }

        try {

            User toKick = event.getMessage().getMentionedUsers().get(0);
            if (toKick.equals(event.getAuthor()) &&
                        !event.getMember().canInteract(event.getGuild().getMember(toKick))) {
                sendMsg(event, "You are not permitted to perform this action.");
                return;
            }
            //Arrays.copyOfRange(Array, From, to)
            String reason = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
            event.getGuild().getController().kick(toKick.getId(), "Kicked by " + event.getAuthor().getName() + "\nReason: " + reason).queue(
                    (noting) -> {
                        AirUtils.modLog(event.getAuthor(), toKick, "kicked", reason, event.getGuild());
                        sendSuccess(event.getMessage());
                    }
            );
        } catch (HierarchyException ignored) { // if we don't do anything with it and just catch it we should name it "ignored"
            //e.printStackTrace();
            sendMsg(event, "I can't kick that member because his roles are above or equals to mine.");
        }


    }

    @Override
    public String help() {
        return "Kicks a user.";
    }

    @Override
    public String getName() {
        return "kick";
    }
}
