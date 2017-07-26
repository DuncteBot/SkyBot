package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SpamCommand implements Command {

    public final static String help = "I'll show you some spam!";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        event.getTextChannel().sendMessage(Functions.embedImage("https://cdn.discordapp.com/attachments/191245668617158656/216896372727742464/spam.jpg")).queue();
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return;
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

}
