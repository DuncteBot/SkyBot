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
import ml.duncte123.skybot.utils.MessageUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class UnbanCommand extends Command {

    public UnbanCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)) {
            MessageUtils.sendMsg(event, "You don't have permission to run this command");
            return;
        }

        if (args.length < 1) {
            MessageUtils.sendMsg(event, "Usage is " + PREFIX + getName() + " <username>");
            return;
        }

        try {
            event.getGuild().getBanList().queue(list -> {
                for(Guild.Ban ban : list) {
                    if(ban.getUser().getName().equalsIgnoreCase(StringUtils.join(args, " "))) {
                        event.getGuild().getController().unban(ban.getUser())
                                .reason("Unbanned by " + event.getAuthor().getName()).queue();
                        MessageUtils.sendMsg(event, "User " + ban.getUser().getName() + " unbanned.");
                        ModerationUtils.modLog(event.getAuthor(), ban.getUser(), "unbanned", event.getGuild());
                        return;
                    }
                }
                MessageUtils.sendMsg(event, "This user is not banned");
            });

        } catch (Exception e) {
            e.printStackTrace();
            MessageUtils.sendMsg(event, "ERROR: " + e.getMessage());
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
