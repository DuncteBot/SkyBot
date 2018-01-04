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

package ml.duncte123.skybot.objects.audioManagers.spotify;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.models.*;
import ml.duncte123.skybot.utils.AirUtils;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyAudioSourceManager implements AudioSourceManager, HttpConfigurable {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyAudioSourceManager.class);

    private static final String PROTOCOL_REGEX = "?:spotify:(track:)|http://|https://[a-z]+\\.";
    private static final String DOMAIN_REGEX = "spotify\\.com/";
    private static final String TRACK_REGEX = "track/";
    private static final String ALBUM_REGEX = "album/";
    private static final String PLAYLIST_REGEX = "user/(.*)/playlist/";
    private static final String REST_REGEX = "(.*)";

    private static final Pattern SPOTIFY_TRACK_REGEX = Pattern.compile("^(" + PROTOCOL_REGEX + DOMAIN_REGEX + TRACK_REGEX + ")" + REST_REGEX +"$");
    private static final Pattern SPOTIFY_ALBUM_REGEX = Pattern.compile("^(" + PROTOCOL_REGEX + DOMAIN_REGEX + ALBUM_REGEX + ")" + REST_REGEX +"$");
    private static final Pattern SPOTIFY_PLAYLIST_REGEX = Pattern.compile("^(" + PROTOCOL_REGEX + DOMAIN_REGEX + ")" + PLAYLIST_REGEX + REST_REGEX +"$");

    private final Api api;
    private final YoutubeSearchProvider youtubeSearchProvider;
    private final YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager();

    public SpotifyAudioSourceManager() {
        String defaultValue = "To use Spotify search, please create an app over at https://developer.spotify.com/web-api/";
        String clientId = AirUtils.config.getString("apis.spotify.clientId", defaultValue);
        String clientSecret = AirUtils.config.getString("apis.spotify.clientSecret", defaultValue);
        if(clientId == null || clientSecret == null || clientId.equals(defaultValue) || clientId.equals(defaultValue)) {
            logger.error("Could not load Spotify keys\n" + defaultValue);
            api = null;
            youtubeSearchProvider = null;
            return;
        }
        youtubeSearchProvider = new YoutubeSearchProvider(youtubeAudioSourceManager);
        api = Api.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
        final ScheduledExecutorService service = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Spotify-Token-Update-Thread"));
        service.scheduleAtFixedRate(this::updateAccessToken, 0, 1, TimeUnit.HOURS);
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        if(isSpotifyAlbum(reference.identifier)) {
            Matcher res = SPOTIFY_ALBUM_REGEX.matcher(reference.identifier);
            if (res.matches()) {

                try {
                    final List<AudioTrack> playList = new ArrayList<>();
                    final Album album = api.getAlbum(res.group(res.groupCount())).build().get();
                    for(SimpleTrack t : album.getTracks().getItems()){
                        String fakeUrl = album.getArtists().get(0).getName() + " - "+ t.getName();
                        playList.add(((AudioPlaylist)youtubeSearchProvider.loadSearchResult(fakeUrl)).getTracks().get(0));
                    }
                    return new BasicAudioPlaylist(album.getName(), playList, playList.get(0), false);
                } catch (Exception e) {
                    //logger.error("Something went wrong!", e);
                    //throw new FriendlyException("DuncteBot: " + e.getMessage(), FriendlyException.Severity.FAULT, e);
                    return null;
                }
            }
        } else if(isSpotifyPlaylist(reference.identifier)) {
            Matcher res = SPOTIFY_PLAYLIST_REGEX.matcher(reference.identifier);
            if (res.matches()) {

                try {
                    final List<AudioTrack> finalPlaylist = new ArrayList<>();
                    final Playlist playlist = api.getPlaylist(res.group(res.groupCount()-1), res.group(res.groupCount())).build().get();
                    for(PlaylistTrack playlistTrack : playlist.getTracks().getItems()){
                        String fakeUrl = playlistTrack.getTrack().getArtists().get(0).getName() + " - " + playlistTrack.getTrack().getName();
                        finalPlaylist.add(((AudioPlaylist)youtubeSearchProvider.loadSearchResult(fakeUrl)).getTracks().get(0));
                    }
                    return new BasicAudioPlaylist(playlist.getName(), finalPlaylist, finalPlaylist.get(0), false);
                } catch (Exception e) {
                    //logger.error("Something went wrong!", e);
                    //throw new FriendlyException("DuncteBot: " + e.getMessage(), FriendlyException.Severity.FAULT, e);
                    return null;
                }
            }
        } else if(isSpotyfyTrack(reference.identifier)) {
            Matcher res = SPOTIFY_TRACK_REGEX.matcher(reference.identifier);
            if (res.matches()) {

                try {
                    final Track track = api.getTrack(res.group(res.groupCount())).build().get();
                    logger.debug("Retrieved track " + track.getName());
                    logger.debug("Its popularity is " + track.getPopularity());

                    if (track.isExplicit()) {
                        logger.debug("This track is explicit!");
                    } else {
                        logger.debug("It's OK, this track isn't explicit.");
                    }
                    return youtubeSearchProvider.loadSearchResult(track.getArtists().get(0).getName() + " - "+ track.getName());
                } catch (Exception e) {
                    //logger.error("Something went wrong!", e);
                    //throw new FriendlyException("DuncteBot: " + e.getMessage(), FriendlyException.Severity.FAULT, e);
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {

    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new SpotifyAudioTrack(trackInfo, youtubeAudioSourceManager);
    }

    @Override
    public void shutdown() {
        youtubeAudioSourceManager.shutdown();
    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {

    }

    private boolean isSpotyfyTrack(String input) {
        return SPOTIFY_TRACK_REGEX.matcher(input).matches();
    }

    private boolean isSpotifyAlbum(String input) {
        return SPOTIFY_ALBUM_REGEX.matcher(input).matches();
    }

    private boolean isSpotifyPlaylist(String input) {
        return SPOTIFY_PLAYLIST_REGEX.matcher(input).matches();
    }

    private void updateAccessToken() {
        Futures.addCallback(api.clientCredentialsGrant().build().getAsync(), new FutureCallback<ClientCredentials>() {
            @Override
            public void onSuccess(ClientCredentials clientCredentials) {
                /* The tokens were retrieved successfully! */
                logger.info("Successfully retrieved an access token! " + clientCredentials.getAccessToken());
                logger.info("The access token expires in " + clientCredentials.getExpiresIn() + " seconds");
                api.setAccessToken(clientCredentials.getAccessToken());
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.error("Something went wrong while loading the token from spotify", throwable);
            }
        });
    }
}
