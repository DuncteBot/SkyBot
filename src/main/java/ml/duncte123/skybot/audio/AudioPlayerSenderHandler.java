package ml.duncte123.skybot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.audio.AudioSendHandler;

public class AudioPlayerSenderHandler implements AudioSendHandler {


    /**
     * This is our audio player
     */
    private final  AudioPlayer audioPlayer;
    /**
     * I don't know what this does but it seems important
     */
    private AudioFrame lastFrame;

    public AudioPlayerSenderHandler(AudioPlayer audioPlayer){
        this.audioPlayer = audioPlayer;
    }

    /**
     * Checks if the player can provide the song
     * @return true if we can provide something
     */
    @Override
    public boolean canProvide() {
      if (lastFrame == null) {
        lastFrame = audioPlayer.provide();
      }
      return lastFrame != null;
    }

    /**
     * This <em>should</em> gives us our audio
     * @return The audio in some nice bytes
     */
    @Override
    public byte[] provide20MsAudio() {

        if(lastFrame == null){
            lastFrame = audioPlayer.provide();
        }

        byte[] data = lastFrame != null ? lastFrame.data : null;
        lastFrame = null;
        return data;
    }

    /**
     * "Checks" if this audio is opus
     * @return always true
     */
    @Override
    public boolean isOpus(){
        return true;
    }

}
