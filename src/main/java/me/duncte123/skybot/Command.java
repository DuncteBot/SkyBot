package me.duncte123.skybot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class Command {
    public abstract boolean called(String[] args, MessageReceivedEvent event);
    public abstract void action(String[] args, MessageReceivedEvent event);
    public abstract String help();
    public void executed(boolean success, MessageReceivedEvent event) {  }

}
