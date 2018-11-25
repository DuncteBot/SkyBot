/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.SinceSkybot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.commands.music.RadioCommand;
import ml.duncte123.skybot.objects.RadioStream;
import ml.duncte123.skybot.objects.TrackUserData;
import ml.duncte123.skybot.objects.audiomanagers.clypit.ClypitAudioSourceManager;
import ml.duncte123.skybot.objects.audiomanagers.speech.SpeechAudioSourceManager;
import ml.duncte123.skybot.objects.audiomanagers.spotify.SpotifyAudioSourceManager;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static me.duncte123.botcommons.messaging.EmbedUtils.embedField;
import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@SinceSkybot(version = "3.5.1")
@Author(nickname = "duncte123", author = "Duncan Sterken")
public class AudioUtils {
    /**
     * This is the title that you see in the embeds from the player
     */
    public final String embedTitle = "AirPlayer";
    /**
     * This will store all the music managers for all the guilds that we are playing music in
     */
    protected final TLongObjectMap<GuildMusicManager> musicManagers;
    /**
     * This will hold the manager for the audio player
     */
    private static AudioPlayerManager playerManager;
    /**
     * This is the default volume that the player will play at
     * I've set it to 100 to save some resources
     */
    private static final int DEFAULT_VOLUME = 100; //(0-150, where 100 is the default max volume)
    private final DunctebotConfig.Apis config;
    private final Variables variables;

    /**
     * This will set everything up and get the player ready
     */
    public AudioUtils(DunctebotConfig.Apis config, Variables variables) {
        java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
        this.config = config;
        this.variables = variables;
        initPlayerManager();
        musicManagers = new TLongObjectHashMap<>();
    }

    private void initPlayerManager() {
        if (playerManager == null) {
            playerManager = new DefaultAudioPlayerManager();
            //playerManager.enableGcMonitoring();

            // Disable cookies for youtube
            YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(true);

            playerManager.registerSourceManager(new SpotifyAudioSourceManager(youtubeAudioSourceManager, config));
            playerManager.registerSourceManager(new ClypitAudioSourceManager());
            playerManager.registerSourceManager(new SpeechAudioSourceManager("en-AU"));

            playerManager.registerSourceManager(youtubeAudioSourceManager);
            playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
            playerManager.registerSourceManager(new BandcampAudioSourceManager());
            playerManager.registerSourceManager(new VimeoAudioSourceManager());
            playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
            playerManager.registerSourceManager(new BeamAudioSourceManager());
            playerManager.registerSourceManager(new HttpAudioSourceManager());

            AudioSourceManagers.registerLocalSource(playerManager);
        }
    }

    public AudioPlayerManager getPlayerManager() {
        initPlayerManager();
        return playerManager;
    }

    /**
     * Loads a track and plays it if the bot isn't playing
     *
     * @param mng
     *         The {@link GuildMusicManager MusicManager} for the guild
     * @param channel
     *         The {@link net.dv8tion.jda.core.entities.MessageChannel channel} that the bot needs to send the messages
     *         to
     * @param trackUrlRaw
     *         The url from the track to play
     * @param addPlayList
     *         If the url is a playlist
     */
    public void loadAndPlay(final GuildMusicManager mng, final TextChannel channel, User requester,
                            final String trackUrlRaw, final CommandContext ctx,
                            final boolean addPlayList) {
        loadAndPlay(mng, channel, requester, trackUrlRaw, addPlayList, ctx, true);
    }

    public void loadAndPlay(final GuildMusicManager mng, final TextChannel channel, User requester, final String trackUrlRaw,
                            final boolean addPlayList,
                            final CommandContext ctx,
                            final boolean announce) {
        final String trackUrl;

        //Strip <>'s that prevent discord from embedding link resources
        if (trackUrlRaw.startsWith("<") && trackUrlRaw.endsWith(">")) {
            trackUrl = trackUrlRaw.substring(1, trackUrlRaw.length() - 1);
        } else {
            trackUrl = trackUrlRaw;
        }

        getPlayerManager().loadItemOrdered(mng, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String title = track.getInfo().title;
                if (track.getInfo().isStream) {
                    Optional<RadioStream> stream = ((RadioCommand) ctx.getCommandManager().getCommand("radio"))
                        .getRadioStreams().stream().filter(s -> s.getUrl().equals(track.getInfo().uri)).findFirst();
                    if (stream.isPresent())
                        title = stream.get().getName();
                }

                track.setUserData(new TrackUserData(requester.getIdLong()));

                mng.scheduler.queue(track);

                if (announce) {
                    String msg = "Adding to queue: " + title;
                    if (mng.player.getPlayingTrack() == null) {
                        msg += "\nand the Player has started playing;";
                    }

                    sendEmbed(channel, embedField(embedTitle, msg));
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                List<AudioTrack> tracks = new ArrayList<>();

                for (final AudioTrack track : playlist.getTracks()) {
                    track.setUserData(new TrackUserData(requester.getIdLong()));
                    tracks.add(track);
                }

                if (tracks.isEmpty()) {
                    sendEmbed(channel, embedField(embedTitle, "Error: This playlist is empty."));
                    return;

                } else if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                if (addPlayList)
                    tracks.forEach(mng.scheduler::queue);
                else
                    mng.scheduler.queue(firstTrack);

                if (announce) {
                    String msg;

                    if (addPlayList) {
                        msg = "Adding **" + playlist.getTracks().size() + "** tracks to queue from playlist: " + playlist.getName();
                        if (mng.player.getPlayingTrack() == null) {
                            msg += "\nand the Player has started playing;";
                        }
                    } else {
                        String prefix = GuildSettingsUtils.getGuild(channel.getGuild(), ctx.getVariables()).getCustomPrefix();
                        msg = "**Hint:** Use `" + prefix + "pplay <playlist link>` to add a playlist." +
                            "\n\nAdding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")";
                        if (mng.player.getPlayingTrack() == null) {
                            msg += "\nand the Player has started playing;";
                        }
                    }
                    sendEmbed(channel, embedField(embedTitle, msg));
                }
            }


            @Override
            public void noMatches() {
                if (announce)
                    sendEmbed(channel, embedField(embedTitle, "Nothing found by _" + trackUrl + "_"));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (!announce) {
                    return;
                }

                if (exception.getMessage().endsWith("Playback on other websites has been disabled by the video owner.")) {
                    sendEmbed(channel, embedField(embedTitle, "Could not play: " + trackUrl
                        + "\nExternal playback of this video was blocked by YouTube."));
                    return;
                }

                Throwable root = ExceptionUtils.getRootCause(exception);

                if (root == null) {
                    // It can return null so shush
                    // noinspection UnusedAssignment
                    root = exception;
                    return;
                }

                sendEmbed(channel, embedField(embedTitle, "Could not play: " + root.getMessage()
                    + "\nIf this happens often try another link or join our [support guild](https://discord.gg/NKM9Xtk) for more!"));

            }
        });
    }

    /**
     * This will get the music manager for the guild or register it if we don't have it yet
     *
     * @param guild
     *         The guild that we need the manager for
     *
     * @return The music manager for that guild
     */
    public GuildMusicManager getMusicManager(Guild guild) {
        GuildMusicManager mng = getMusicManager(guild, true);
        guild.getAudioManager().setSendingHandler(mng.getSendHandler());
        return mng;
    }

    public GuildMusicManager getMusicManager(Guild guild, boolean createIfNull) {
        long guildId = guild.getIdLong();
        GuildMusicManager mng = musicManagers.get(guildId);
        if (mng == null) {
            synchronized (musicManagers) {
                mng = musicManagers.get(guildId);
                if (mng == null && createIfNull) {
                    mng = new GuildMusicManager(guild, variables);
                    mng.player.setVolume(DEFAULT_VOLUME);
                    musicManagers.put(guildId, mng);
                }
            }
        }
        return mng;
    }

    public TLongObjectMap<GuildMusicManager> getMusicManagers() {
        return musicManagers;
    }

    /**
     * This will return the formatted timestamp for the current playing track
     *
     * @param milliseconds
     *         the milliseconds that the track is at
     *
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
}
