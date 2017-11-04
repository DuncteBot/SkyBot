/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class UnbanCommand extends Command {

    public UnbanCommand() {
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

        if (args[0].isEmpty()) {
            sendMsg(event, "Usage is " + Settings.prefix + getName() +" <username>");
            return;
        }

        try {
            Guild guild = event.getGuild();
            List<User> bannedUsers =  guild.getBans().complete();
            for (User bannedUser : bannedUsers) {
                if (bannedUser.getName().equalsIgnoreCase(args[0])) {
                    guild.getController().unban(bannedUser).reason("Unbanned by " + event.getAuthor().getName()).queue();
                    event.getChannel().sendMessage("User " + bannedUser.getName() + " unbanned.").queue();
                    AirUtils.modLog(event.getAuthor(), bannedUser, "unbanned", event.getGuild());
                    return;
                }
            }
            event.getChannel().sendMessage("This user is not banned").queue();
        }
        catch (Exception e) {
            e.printStackTrace();
            sendMsg(event, "ERROR: " + e.getMessage());
        }
    }

    @Override
    public String help() {
        return "Unbans a user";
    }

    @Override
    public String getName() {
        return "unban";
    }
}
