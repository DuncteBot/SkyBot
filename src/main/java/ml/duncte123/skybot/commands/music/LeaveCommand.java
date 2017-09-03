package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.commands.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.time.Instant;

public class LeaveCommand extends Command {

    public final static String help = "make the bot leave your channel.";

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        boolean botInChannel = false;

        if(event.getGuild().getAudioManager().isConnected()){
            botInChannel = true;

            if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
                event.getChannel().sendMessage(AirUtils.embedField(SkyBot.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands")).queue();
                return false;
            }

        }else{
            event.getChannel().sendMessage(AirUtils.embedField(SkyBot.au.embedTitle, "I'm not in a channel atm")).queue();
        }


        return botInChannel;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        SkyBot.au.getMusicManager(event.getGuild()).player.stopTrack();
        event.getGuild().getAudioManager().setSendingHandler(null);
        event.getGuild().getAudioManager().closeAudioConnection();
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Config.defaultColour)
                .addField(SkyBot.au.embedTitle, "Leaving your channel", false)
                .setFooter(Config.defaultName, Config.defaultIcon)
                .setTimestamp(Instant.now());
        event.getChannel().sendMessage(eb.build()).queue();

    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }
}
