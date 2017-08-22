package ml.duncte123.skybot.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.duncte123.skybot.audio.GuildMusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class AudioUtils {

    private static final int DEFAULT_VOLUME = 35; //(0-150, where 100 is the default max volume)

    private final AudioPlayerManager playerManager;
    private final Map<String, GuildMusicManager> musicManagers;

    public final String embedTitle = Config.playerTitle;

    public AudioUtils(){
        java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);

        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        //playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());

        //AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        musicManagers = new HashMap<>();
    }

    /**
     * Loads a track and plays it if the bot isn't playing
     * @param mng The {@link GuildMusicManager MusicManager} for the guild
     * @param channel The {@link net.dv8tion.jda.core.entities.MessageChannel channel} that the bot needs to send the messages to
     * @param trackUrl The url from the track to play
     * @param addPlayList If the url is a playlist
     */
    public void loadAndPlay(GuildMusicManager mng, final MessageChannel channel, final String trackUrl, final boolean addPlayList){
        playerManager.loadItemOrdered(mng, trackUrl, new AudioLoadResultHandler(){

            /**
             * fires when a track is loaded
             * @param track The current {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack track} that has been loaded
             */
            @Override
            public void trackLoaded(AudioTrack track) {
                String msg = "Adding to queue: " + track.getInfo().title;
                if(mng.player.getPlayingTrack() == null){
                    msg += "\nand the Player has started playing;";
                }

                mng.scheduler.queue(track);
                channel.sendMessage(AirUtils.embedField(embedTitle, msg)).queue();

            }

            /**
             * Fires when a playlist is loaded
             * @param playlist The {@link com.sedmelluq.discord.lavaplayer.track.AudioPlaylist playlist} that has been loaded
             */
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                List<AudioTrack> tracks = playlist.getTracks();

                if(firstTrack == null){
                    firstTrack = playlist.getTracks().get(0);
                }
                String msg = "";

                if(addPlayList){
                    msg = "Adding **"+playlist.getTracks().size()+"** tracks to queue from playlist: "+playlist.getName();
                    tracks.forEach(mng.scheduler::queue);
                }else{
                    msg = "Adding to queue "+ firstTrack.getInfo().title+" (first track of playlist "+playlist.getName()+")";
                    mng.scheduler.queue(firstTrack);
                }
                if(mng.player.getPlayingTrack() == null){
                    msg += "\nand the Player has started playing;";
                }
                channel.sendMessage(AirUtils.embedField(embedTitle, msg)).queue();
            }

            /**
             * When noting is found for the search
             */
            @Override
            public void noMatches() {
                channel.sendMessage(AirUtils.embedField(embedTitle, "Nothing found by ["+trackUrl+"]("+trackUrl+")")).queue();
            }

            /**
             * When something broke and you need to scream at <em>duncte123#1245</em>
             * @param exception A {@link com.sedmelluq.discord.lavaplayer.tools.FriendlyException FriendlyException}
             */
            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage(AirUtils.embedField(embedTitle, "Could not play: "+exception.getMessage())).queue();

            }

        });
    }



    public synchronized GuildMusicManager getMusicManager(Guild guild){
        String guildId = guild.getId();
        GuildMusicManager mng = musicManagers.get(guildId);
            if(mng == null){
                mng = new GuildMusicManager(playerManager);
                mng.player.setVolume(DEFAULT_VOLUME);
                musicManagers.put(guildId, mng);
            }

        guild.getAudioManager().setSendingHandler(mng.getSendHandler());

        return mng;
    }

    public static String getTimestamp(long miliseconds){
        int seconds = (int) (miliseconds / 1000) % 60;
        int minutes = (int) ((miliseconds / (1000 * 60)) % 60);
        int hours = (int) ((miliseconds / (1000 * 60 * 60)) % 24);

        if(hours > 0){
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }else{
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

}
