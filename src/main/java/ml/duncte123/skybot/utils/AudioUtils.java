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

import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import fredboat.audio.player.LavalinkManager;
import gnu.trove.map.TLongObjectMap;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.SinceSkybot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.audio.AudioLoader;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.audio.UserContextAudioPlayerManager;
import ml.duncte123.skybot.audio.sourcemanagers.YoutubeAudioSourceManagerOverride;
import ml.duncte123.skybot.objects.audiomanagers.clypit.ClypitAudioSourceManager;
import ml.duncte123.skybot.objects.audiomanagers.speech.SpeechAudioSourceManager;
import ml.duncte123.skybot.objects.audiomanagers.spotify.SpotifyAudioSourceManager;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;

import java.util.concurrent.Future;

@SinceSkybot(version = "3.5.1")
@Author(nickname = "duncte123", author = "Duncan Sterken")
public class AudioUtils {
    public final String embedTitle = "AirPlayer";
    protected final TLongObjectMap<GuildMusicManager> musicManagers;
    private final Variables variables;
    private UserContextAudioPlayerManager playerManager;
    // public so we can change it with eval
    @SuppressWarnings("WeakerAccess")
    public static String YOUTUBE_VERSION = "2.20191122.05.01";

    public AudioUtils(DunctebotConfig.Apis config, Variables variables) {
        this.variables = variables;
        musicManagers = MapUtils.newLongObjectMap();

        playerManager = new UserContextAudioPlayerManager();
        //playerManager.enableGcMonitoring();

        final YoutubeAudioSourceManagerOverride youtubeAudioSourceManager = new YoutubeAudioSourceManagerOverride(
            false,
            variables.getYoutubeCache(),
            config.googl
        );

        youtubeAudioSourceManager.getMainHttpConfiguration().setHttpContextFilter(new YoutubeContextFilterOverride());

        playerManager.registerSourceManager(new SpotifyAudioSourceManager(youtubeAudioSourceManager, config));
        playerManager.registerSourceManager(new ClypitAudioSourceManager());
        playerManager.registerSourceManager(new SpeechAudioSourceManager("en-AU"));

        playerManager.registerSourceManager(youtubeAudioSourceManager);
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
    }

    public UserContextAudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public void loadAndPlay(final GuildMusicManager mng, final String trackUrlRaw, final CommandContext ctx) {
        loadAndPlay(mng, trackUrlRaw, ctx, true);
    }

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

    public GuildMusicManager getMusicManager(Guild guild) {
        final long guildId = guild.getIdLong();
        GuildMusicManager mng = musicManagers.get(guildId);

        if (mng == null) {
            synchronized (musicManagers) {
                mng = musicManagers.get(guildId);

                if (mng == null) {
                    mng = new GuildMusicManager(guild, variables);
                    musicManagers.put(guildId, mng);
                }
            }
        }

        if (!LavalinkManager.ins.isEnabled() && guild.getAudioManager().getSendingHandler() == null) {
            guild.getAudioManager().setSendingHandler(mng.getSendHandler());
        }

        return mng;
    }

    public TLongObjectMap<GuildMusicManager> getMusicManagers() {
        return musicManagers;
    }

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

    private static class YoutubeContextFilterOverride extends YoutubeHttpContextFilter {
        @Override
        public void onRequest(HttpClientContext context, HttpUriRequest request, boolean isRepetition) {
            super.onRequest(context, request, isRepetition);

            request.setHeader("x-youtube-client-version", YOUTUBE_VERSION);
        }
    }
}
