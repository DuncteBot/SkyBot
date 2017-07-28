package me.duncte123.skybot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface Command {
    boolean called(String[] args, MessageReceivedEvent event);
    void action(String[] args, MessageReceivedEvent event);
    String help();
    void executed(boolean success, MessageReceivedEvent event);

}
