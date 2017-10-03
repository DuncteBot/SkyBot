package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class LeaveCommand extends Command {

    public final static String help = "make the bot leave your channel.";

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if(event.getGuild().getAudioManager().isConnected()){
            if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
                sendMsg(event,"I'm sorry, but you have to be in the same channel as me to use any music related commands");
                return;
            }

        }else{
            sendMsg(event,"I'm not in a channel atm");
            return;
        }

        if(event.getGuild().getAudioManager().isConnected()) {
            AirUtils.audioUtils.getMusicManager(event.getGuild()).player.stopTrack();
            event.getGuild().getAudioManager().setSendingHandler(null);
            event.getGuild().getAudioManager().closeAudioConnection();
           sendEmbed(EmbedUtils.embedField(AirUtils.audioUtils.embedTitle, "Leaving your channel"), event);
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
