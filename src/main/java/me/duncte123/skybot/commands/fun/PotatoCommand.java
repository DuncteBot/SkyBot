package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.Command;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PotatoCommand extends Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        event.getTextChannel().sendMessage((new MessageBuilder()).setTTS(true).append("potato").build()).queue();
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "POTATO!!!!";
    }
}
