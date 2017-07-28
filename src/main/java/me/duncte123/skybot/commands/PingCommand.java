package me.duncte123.skybot.commands;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PingCommand implements Command {

    public final static String help = "PONG!";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        long time = System.currentTimeMillis();

        event.getTextChannel().sendMessage("PONG!").queue( (message) ->
            message.editMessageFormat("PONG!\nping is: %dms \nWebsocket ping: " + event.getJDA().getPing() + "ms", (System.currentTimeMillis() - time) ).queue());

    }

    @Override
    public String help() {
        return help;
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        return;
    }


}
