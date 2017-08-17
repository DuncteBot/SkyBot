package ml.duncte123.skybot.commands;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.CustomLog;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CleenupCommand extends Command {

    private int deletedMsg = 0;

    public final static String help = "performs a cleanup in the channel where the command is run. (MOD or higher ONLY!)";
    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        Permission[] permissions = {
                Permission.MESSAGE_MANAGE,
                Permission.MESSAGE_HISTORY
        };

        if(event.getAuthor().isBot()){return false;}
        if(!PermissionUtil.checkPermission(event.getMember(), permissions )){
            event.getTextChannel().sendMessage(AirUtils.embedMessage("You don't have permission to run this command!")).queue();
            return false;
        }
        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        int total = 5;

        if(args.length > 0){
            total = Integer.parseInt(args[0]);
          if (total < 2 || total > 100) {
            event.getTextChannel().sendMessage(AirUtils.embedMessage("Error: count must be minimal 2 and maximal 100")).queue(
               (message) -> { message.delete().queueAfter(5, TimeUnit.SECONDS); }
            );
      return;
          }
            }
    
        try {
          MessageHistory mh = new MessageHistory(event.getTextChannel());
          List<Message> msgLst =  mh.retrievePast(total).complete();
          event.getTextChannel().deleteMessages(msgLst).queue();
          deletedMsg = msgLst.size();
                event.getTextChannel().sendMessage(AirUtils.embedMessage("Removed "+deletedMsg+" messages!")).queue(
             (message) -> { message.delete().queueAfter(5, TimeUnit.SECONDS); }
          );
                AirUtils.log(CustomLog.Level.INFO, deletedMsg+" removed in channel "+event.getTextChannel().getName());
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

}
