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
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.event.Level;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CleenupCommand extends Command {

    public CleenupCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    public final static String help = "performs a cleanup in the channel where the command is run.";

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {


        Permission[] permissions = {
                Permission.MESSAGE_MANAGE,
                Permission.MESSAGE_HISTORY
        };
        if(!event.getMember().hasPermission(permissions)){
            sendMsg(event, "You don't have permission to run this command!");
            return;
        }

        int deletedMsg = 0;
        int total = 5;

        if(args.length > 0){
            total = Integer.parseInt(args[0]);
          if (total < 2 || total > 100) {
                event.getChannel().sendMessage("Error: count must be minimal 2 and maximal 100").queue(
               message -> message.delete().queueAfter(5, TimeUnit.SECONDS)
            );
            return;
          }
        }
    
        try {
          MessageHistory mh = event.getChannel().getHistory();
          List<Message> msgLst =  mh.retrievePast(total).complete();
          event.getChannel().deleteMessages(msgLst).queue();
          deletedMsg = msgLst.size();
                event.getChannel().sendMessage("Removed "+deletedMsg+" messages!").queue(
             message -> message.delete().queueAfter(5, TimeUnit.SECONDS)
          );
                AirUtils.log(Level.INFO, deletedMsg+" messages removed in channel "+event.getChannel().getName());
        }
        catch (Exception e) {
          event.getChannel().sendMessage("ERROR: " + e.getMessage()).queue();
        }
    }

    @Override
    public String help() {
        return help;
    }

    @Override
    public String getName() {
        return "cleanup";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"clear"};
    }
}
