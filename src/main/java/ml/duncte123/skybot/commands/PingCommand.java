package ml.duncte123.skybot.commands;

import ml.duncte123.skybot.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PingCommand extends Command {

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
}
