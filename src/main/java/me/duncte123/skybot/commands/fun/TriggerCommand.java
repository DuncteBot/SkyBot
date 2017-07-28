package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class TriggerCommand implements Command {

    public final static String help = "use when you are triggered.";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
                event.getTextChannel().sendMessage(AirUtils.embedImage("https://cdn.discordapp.com/attachments/94831883505905664/176181155467493377/triggered.gif")).queue();
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
