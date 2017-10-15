/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.event.Level;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CleenupCommand extends Command {

    public final static String help = "performs a cleanup in the channel where the command is run.";
    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {


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

    /**
     * The usage instructions of the command
     * @return a String
     */
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
