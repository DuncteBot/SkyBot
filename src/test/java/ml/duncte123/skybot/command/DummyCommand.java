package ml.duncte123.skybot.command;

import ml.duncte123.skybot.objects.command.Command;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * This is a dummy command to test some things with commands
 */
public class DummyCommand extends Command {
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        //Do nothing
    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public String getName() {
        return "dummy";
    }
}
