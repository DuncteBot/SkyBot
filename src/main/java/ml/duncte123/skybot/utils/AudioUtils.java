/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.duncte123.skybot.SinceSkybot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.audioManagers.clypit.ClypitAudioSourceManager;
import ml.duncte123.skybot.objects.audioManagers.spotify.SpotifyAudioSourceManager;
import ml.duncte123.skybot.objects.command.Command;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@SinceSkybot(version = "3.5.1")
public class AudioUtils {

    /**
     * This is the default volume that the player will play at
     * I've set it to 100 to save some resources
     */
    private static final int DEFAULT_VOLUME = 100; //(0-150, where 100 is the default max volume)

    /**
     * This is the title that you see in the embeds from the player
     */
    public final String embedTitle = "AirPlayer";
    /**
     * This will hold the manager for the audio player
     */
    private final AudioPlayerManager playerManager;

    /**
     * This will store all the music managers for all the guilds that we are playing music in
     */
    final Map<String, GuildMusicManager> musicManagers;

    /**
     * This will set everything up and get the player ready
     */
    AudioUtils() {
        java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
        
        this.playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new SpotifyAudioSourceManager());
        playerManager.registerSourceManager(new ClypitAudioSourceManager());
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        
        musicManagers = new HashMap<>();
    }

    /**
     * This will return the formatted timestamp for the current playing track
     *
     * @param milliseconds the milliseconds that the track is at
     * @return a formatted time
     */
    public static String getTimestamp(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Loads a track and plays it if the bot isn't playing
     *
     * @param mng         The {@link GuildMusicManager MusicManager} for the guild
     * @param channel     The {@link net.dv8tion.jda.core.entities.MessageChannel channel} that the bot needs to send the messages to
     * @param trackUrlRaw The url from the track to play
     * @param addPlayList If the url is a playlist
     */
    public void loadAndPlay(GuildMusicManager mng, final TextChannel channel, final String trackUrlRaw, final boolean addPlayList) {
        final String trackUrl;
        
        //Strip <>'s that prevent discord from embedding link resources
        if (trackUrlRaw.startsWith("<") && trackUrlRaw.endsWith(">")) {
            trackUrl = trackUrlRaw.substring(1, trackUrlRaw.length() - 1);
        } else {
            trackUrl = trackUrlRaw;
        }
        
        playerManager.loadItemOrdered(mng, trackUrl, new AudioLoadResultHandler() {
            
            /**
             * fires when a track is loaded
             * @param track The current {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack track} that has been loaded
             */
            @Override
            public void trackLoaded(AudioTrack track) {
                String msg = "Adding to queue: " + track.getInfo().title;
                if (mng.player.getPlayingTrack() == null) {
                    msg += "\nand the Player has started playing;";
                }
                
                mng.scheduler.queue(track);
                sendEmbed(EmbedUtils.embedField(embedTitle, msg), channel);
                
            }
            
            /**
             * Fires when a playlist is loaded
             * @param playlist The {@link com.sedmelluq.discord.lavaplayer.track.AudioPlaylist playlist} that has been loaded
             */
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                List<AudioTrack> tracks = playlist.getTracks();

                if(tracks.size() == 0) {
                    sendEmbed(EmbedUtils.embedField(embedTitle, "Error: This playlist is empty."), channel);
                    return;

                } else if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                String msg;
                
                if (addPlayList) {
                    msg = "Adding **" + playlist.getTracks().size() + "** tracks to queue from playlist: " + playlist.getName();
                    if (mng.player.getPlayingTrack() == null) {
                        msg += "\nand the Player has started playing;";
                    }
                    tracks.forEach(mng.scheduler::queue);
                } else {
                    msg = "Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")";
                    if (mng.player.getPlayingTrack() == null) {
                        msg += "\nand the Player has started playing;";
                    }
                    mng.scheduler.queue(firstTrack);
                }
                sendEmbed(EmbedUtils.embedField(embedTitle, msg), channel);
            }
            
            /**
             * When noting is found for the search
             */
            @Override
            public void noMatches() {
                sendEmbed(EmbedUtils.embedField(embedTitle, "Nothing found by _" + trackUrl + "_"), channel);
            }
            
            /**
             * When something broke and you need to scream at <em>duncte123#1245</em>
             * @param exception A {@link com.sedmelluq.discord.lavaplayer.tools.FriendlyException FriendlyException}
             */
            @Override
            public void loadFailed(FriendlyException exception) {
                sendEmbed(EmbedUtils.embedField(embedTitle, "Could not play: " + exception.getMessage() + "\nIf this happens often try another link or join our [support guild](https://discord.gg/NKM9Xtk) for more!"), channel);
            }
        });
    }

    /**
     * This will get the music manager for the guild or register it if we don't have it yet
     *
     * @param guild The guild that we need the manager for
     * @return The music manager for that guild
     */
    public synchronized GuildMusicManager getMusicManager(Guild guild) {
        String guildId = guild.getId();
        GuildMusicManager mng = musicManagers.get(guildId);
        if (mng == null) {
            synchronized (musicManagers) {
                mng = musicManagers.get(guildId);
                if (mng == null) {
                    mng = new GuildMusicManager(playerManager);
                    mng.player.setVolume(DEFAULT_VOLUME);
                    musicManagers.put(guildId, mng);
                }
            }
        }
        
        guild.getAudioManager().setSendingHandler(mng.getSendHandler());
        
        return mng;
    }

    /**
     * {@link Command#sendEmbed(GuildMessageReceivedEvent, MessageEmbed)}
     *
     * @param embed   {@link Command#sendEmbed(GuildMessageReceivedEvent, MessageEmbed)}
     * @param tc {@link Command#sendEmbed(GuildMessageReceivedEvent, MessageEmbed)}
     */
    private void sendEmbed(MessageEmbed embed, TextChannel tc) {
        if(tc.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) {
            if (!tc.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_EMBED_LINKS)) {
                tc.sendMessage(EmbedUtils.embedToMessage(embed)).queue();
                return;
            }
            tc.sendMessage(embed).queue();
        }
    }

    public Map<String, GuildMusicManager> getMusicManagers() {
        return musicManagers;
    }
}
