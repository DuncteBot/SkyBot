package me.duncte123.skybot.commands;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.HelpEmbeds;
import net.dv8tion.jda.core.entities.TextChannel;
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
            pc.sendMessage(HelpEmbeds.mainCommands).queue( msg -> {
                        pc.sendMessage(HelpEmbeds.musicCommands).queue();
                        pc.sendMessage(HelpEmbeds.funCommands).queue();
                        pc.sendMessage(HelpEmbeds.modCommands).queue();
                        event.getTextChannel().sendMessage(event.getMember().getAsMention() +" check your DM's").queue();
                    },
                    //When sending fails, send to the channel
                    err -> {
                        TextChannel currentChann = event.getTextChannel();
                        currentChann.sendMessage(HelpEmbeds.mainCommands).queue();
                        currentChann.sendMessage(HelpEmbeds.musicCommands).queue();
                        currentChann.sendMessage(HelpEmbeds.funCommands).queue();
                        currentChann.sendMessage(HelpEmbeds.modCommands).queue();
                        currentChann.sendMessage("Message could not be delivered to dm's and has been send in this channel.").queue();
                    }
            );
        },
                err -> event.getChannel().sendMessage("ERROR: " + err.getMessage()).queue()
        );
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }
}
