package ml.duncte123.skybot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {

    /**
     * This is our player
     */
    public final AudioPlayer player;
    /**
     * This is the scheduler
     */
    public final TrackScheduler scheduler;
    /**
     * This is what actually sends the audio
     */
    public final AudioPlayerSenderHandler sendHandler;

    /**
     *
     * @param manager The {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager AudioPlayerManager} for the corresponding guild
     */
    public GuildMusicManager(AudioPlayerManager manager){
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        sendHandler = new AudioPlayerSenderHandler(player);
        player.addListener(scheduler);
    }

    /**
     * This will get our sendings handler
     * @return The {@link ml.duncte123.skybot.audio.AudioPlayerSenderHandler thing} that sends our audio
     */
    public AudioPlayerSenderHandler getSendHandler(){
        return sendHandler;
    }

}
