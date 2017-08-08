package me.duncte123.skybot.commands;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.HelpEmbeds;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class HelpCommand extends Command {

    public final static String help = "shows a list of all the commands.";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {


        event.getAuthor().openPrivateChannel().queue( (pc) -> {
            event.getTextChannel().sendMessage(event.getMember().getAsMention() +" check your DM's").queue();
            pc.sendMessage(HelpEmbeds.mainCommands).queue();
            pc.sendMessage(HelpEmbeds.musicCommands).queue();
            pc.sendMessage(HelpEmbeds.funCommands).queue();
            pc.sendMessage(HelpEmbeds.modCommands).queue();
        },
                err -> event.getChannel().sendMessage("ERROR: " + err.getMessage()).queue() );
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }
}
