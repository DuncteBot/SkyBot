package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
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
        if(!PermissionUtil.checkPermission(event.getMember(), permissions )){
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
          MessageHistory mh = new MessageHistory(event.getChannel());
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
