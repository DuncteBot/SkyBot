package me.duncte123.skybot.commands;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.utils.CustomLog;
import me.duncte123.skybot.utils.AirUtils;
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
                SkyBot.log(CustomLog.Level.INFO, deletedMsg+" removed in channel "+event.getTextChannel().getName());
        }
        catch (Exception e) {
          event.getChannel().sendMessage("ERROR: " + e.getMessage()).queue();
        }
    }

    @Override
    public String help() {
        return help;
    }

}
