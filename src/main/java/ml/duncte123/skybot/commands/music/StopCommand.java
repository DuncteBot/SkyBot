package ml.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.audio.TrackScheduler;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class StopCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if(!event.getGuild().getAudioManager().isConnected()){
            sendMsg(event, "I'm not in a voice channel, use `"+Config.prefix+"join` to make me join a channel");
            return;
        }

        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            sendMsg(event, "I'm sorry, but you have to be in the same channel as me to use any music related commands");
            return;
        }

        AudioUtils au = AirUtils.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        AudioPlayer player = mng.player;
        TrackScheduler scheduler = mng.scheduler;

        if(mng.player.getPlayingTrack() == null){
            sendMsg(event, "The player is not playing.");
            return;
        }

        scheduler.queue.clear();
        player.stopTrack();
        player.setPaused(false);
        sendMsg(event, "Playback has been completely stopped and the queue has been cleared");
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "stops the music player.";
    }

    @Override
    public String getName() {
        return "stop";
    }

}
