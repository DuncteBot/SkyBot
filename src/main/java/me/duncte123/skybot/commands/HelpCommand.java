package me.duncte123.skybot.commands;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.SortedSet;
import java.util.TreeSet;

public class HelpCommand implements Command {

    public final static String help = "shows a list of all the commands.";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        EmbedBuilder eb = AirUtils.defaultEmbed();

        SortedSet<String> commands = new TreeSet<String>(SkyBot.commands.keySet());
        for(String cmd: commands){
            if((!SkyBot.commands.get(cmd).help().isEmpty()) || (SkyBot.commands.get(cmd).help() != null)){
                eb.addField(Config.prefix+cmd, SkyBot.commands.get(cmd).help(), false);
            }
        }

        event.getTextChannel().sendMessage(event.getMember().getAsMention() +" check your DM's").queue();

        event.getAuthor().openPrivateChannel().queue( pc -> pc.sendMessage(eb.build()).queue());
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return;

    }

}
