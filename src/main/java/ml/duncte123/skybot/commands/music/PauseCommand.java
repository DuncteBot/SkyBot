package ml.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class PauseCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            sendMsg(event, "I'm sorry, but you have to be in the same channel as me to use any music related commands");
            return;
        }

        AudioUtils au = AirUtils.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        AudioPlayer player = mng.player;

        if (player.getPlayingTrack() == null){
            sendMsg(event, "Cannot pause or resume player because no track is loaded for playing.");
            return;
        }

        player.setPaused(!player.isPaused());
        if (player.isPaused()){
            sendMsg(event, "The player has been paused.");
        }else{
            sendMsg(event, "The player has resumed playing.");
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "pauses the current song";
    }

    @Override
    public String getName() {
        return "pause";
    }
}
