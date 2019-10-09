/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import fredboat.audio.player.LavalinkManager;
import gnu.trove.map.TLongObjectMap;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.SinceSkybot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.audio.AudioLoader;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.audio.UserContextAudioPlayerManager;
import ml.duncte123.skybot.objects.audiomanagers.clypit.ClypitAudioSourceManager;
import ml.duncte123.skybot.objects.audiomanagers.speech.SpeechAudioSourceManager;
import ml.duncte123.skybot.objects.audiomanagers.spotify.SpotifyAudioSourceManager;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.Future;
import java.util.logging.Level;

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
    private UserContextAudioPlayerManager playerManager;
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
        musicManagers = MapUtils.newLongObjectMap();
    }

    private void initPlayerManager() {
        if (playerManager == null) {
            playerManager = new UserContextAudioPlayerManager();
            //playerManager.enableGcMonitoring();

            final YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(false);

            // When the values change
            /*youtubeAudioSourceManager.setHttpRequestModifier((request) -> {
                request.setHeader("x-youtube-client-name", "1");
                request.setHeader("x-youtube-client-version", "2.20191008.04.01");
            });*/

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

    public UserContextAudioPlayerManager getPlayerManager() {
        initPlayerManager();
        return playerManager;
    }

    /**
     * Loads a track and plays it if the bot isn't playing
     *
     * @param mng
     *         The {@link GuildMusicManager MusicManager} for the guild
     * @param trackUrlRaw
     *         The url from the track to play
     * @param ctx
     *         the command context for this event
     */
    public void loadAndPlay(final GuildMusicManager mng, final String trackUrlRaw, final CommandContext ctx) {
        loadAndPlay(mng, trackUrlRaw, ctx, true);
    }

    /**
     * Loads a track and plays it if the bot isn't playing
     *
     * @param mng
     *         The {@link GuildMusicManager MusicManager} for the guild
     * @param trackUrlRaw
     *         The url from the track to play
     * @param ctx
     *         the command context for this event
     * @param announce
     *         if we should announce the track
     *
     * @return The future from lavaplayer
     */
    public Future<Void> loadAndPlay(final GuildMusicManager mng, final String trackUrlRaw,
                                    final CommandContext ctx, final boolean announce) {
        final boolean isPatron = CommandUtils.isUserTagPatron(ctx.getAuthor());
        final String trackUrl;

        //Strip <>'s that prevent discord from embedding link resources
        if (trackUrlRaw.startsWith("<") && trackUrlRaw.endsWith(">")) {
            trackUrl = trackUrlRaw.substring(1, trackUrlRaw.length() - 1);
        } else {
            trackUrl = trackUrlRaw;
        }

        final AudioLoader loader = new AudioLoader(ctx, mng, announce, trackUrl, this, isPatron);

        return getPlayerManager().loadItemOrdered(mng, trackUrl, loader, isPatron);
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
        final long guildId = guild.getIdLong();
        GuildMusicManager mng = musicManagers.get(guildId);

        if (mng == null) {
            synchronized (musicManagers) {
                mng = musicManagers.get(guildId);

                if (mng == null) {
                    mng = new GuildMusicManager(guild, variables);
                    mng.player.setVolume(DEFAULT_VOLUME);
                    musicManagers.put(guildId, mng);
                }
            }
        }

        if (!LavalinkManager.ins.isEnabled()) {
            guild.getAudioManager().setSendingHandler(mng.getSendHandler());
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
        final int seconds = (int) (milliseconds / 1000) % 60;
        final int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        final int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
