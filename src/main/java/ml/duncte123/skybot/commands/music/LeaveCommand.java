package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

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
               sendMsg(event, AirUtils.embedField(AirUtils.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands"));
                return false;
            }

        }else{
            sendMsg(event, AirUtils.embedField(AirUtils.au.embedTitle, "I'm not in a channel atm"));
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
        if(event.getGuild().getAudioManager().isConnected()) {
            AirUtils.au.getMusicManager(event.getGuild()).player.stopTrack();
            event.getGuild().getAudioManager().setSendingHandler(null);
            event.getGuild().getAudioManager().closeAudioConnection();
            sendMsg(event, AirUtils.embedField(AirUtils.au.embedTitle, "Leaving your channel"));
        } else {
            sendMsg(event, "I'm not connected to any channels.");
        }


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

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"disconnect"};
    }
}
