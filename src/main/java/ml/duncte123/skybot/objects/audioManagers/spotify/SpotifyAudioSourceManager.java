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
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.models.*;
import ml.duncte123.skybot.utils.AirUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

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
    private final YoutubeAudioSourceManager youtubeAudioSourceManager;
    private final SoundCloudAudioSourceManager manager = null;
    private final ScheduledExecutorService service;

    //    public SpotifyAudioSourceManager(SoundCloudAudioSourceManager manager) {
    public SpotifyAudioSourceManager(YoutubeAudioSourceManager youtubeAudioSourceManager) {
        String defaultValue = "To use Spotify search, please create an app over at https://developer.spotify.com/web-api/";
        String clientId = AirUtils.config.getString("apis.spotify.clientId", defaultValue);
        String clientSecret = AirUtils.config.getString("apis.spotify.clientSecret", defaultValue);
        if(clientId == null || clientSecret == null || clientId.equals(defaultValue) || clientId.equals(defaultValue)) {
            logger.error("Could not load Spotify keys\n" + defaultValue);
            this.api = null;
            //this.manager = null;
            youtubeSearchProvider = null;
            this.service = null;
            this.youtubeAudioSourceManager = null;
        } else {
            //this.manager = manager;
            this.youtubeAudioSourceManager = youtubeAudioSourceManager;
            youtubeSearchProvider = new YoutubeSearchProvider(youtubeAudioSourceManager);
            this.api = Api.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
            this.service = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Spotify-Token-Update-Thread"));
            service.scheduleAtFixedRate(this::updateAccessToken, 0, 1, TimeUnit.HOURS);
        }
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        if(this.youtubeAudioSourceManager == null)
            return null;

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
                    return youtubeSearchProvider.loadSearchResult(track.getArtists().get(0).getName() + " - "+ track.getName());
                } catch (Exception e) {
                    //logger.error("Something went wrong!", e);
                    //throw new FriendlyException("DuncteBot: " + e.getMessage(), FriendlyException.Severity.FAULT, e);
                    return null;
                }
            }
        }
        return null;

        /*if(this.manager == null)
            return null;

        if(isSpotifyAlbum(reference.identifier)) {
            Matcher res = SPOTIFY_ALBUM_REGEX.matcher(reference.identifier);
            if (res.matches()) {

                try {
                    final List<AudioTrack> playList = new ArrayList<>();
                    final Album album = api.getAlbum(res.group(res.groupCount())).build().get();
                    for(SimpleTrack t : album.getTracks().getItems()){
                        String fakeUrl = album.getArtists().get(0).getName() + " - "+ t.getName();
                        playList.add( ((AudioPlaylist) loadSearchResult(fakeUrl, 0, 1)).getTracks().get(0) );
                    }
                    return new BasicAudioPlaylist(album.getName(), playList, playList.get(0), false);
                } catch (Exception e) {
                    logger.error("Something went wrong!", e);
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
                        List<AudioTrack> playlist1 = ((AudioPlaylist)loadSearchResult(fakeUrl, 0, 1)).getTracks();
                        if(playlist1.size() > 0)
                            finalPlaylist.add(playlist1.get(0));
                    }
                    return new BasicAudioPlaylist(playlist.getName(), finalPlaylist, finalPlaylist.get(0), false);
                } catch (Exception e) {
                    logger.error("Something went wrong!", e);
                    //throw new FriendlyException("DuncteBot: " + e.getMessage(), FriendlyException.Severity.FAULT, e);
                    return null;
                }
            }
        } else if(isSpotyfyTrack(reference.identifier)) {
            Matcher res = SPOTIFY_TRACK_REGEX.matcher(reference.identifier);
            if (res.matches()) {

                try {
                    final Track track = api.getTrack(res.group(res.groupCount())).build().get();
                    return loadSearchResult(track.getArtists().get(0).getName() + " - "+ track.getName(), 0, 1);
                } catch (Exception e) {
                    logger.error("Something went wrong!", e);
                    //throw new FriendlyException("DuncteBot: " + e.getMessage(), FriendlyException.Severity.FAULT, e);
                    return null;
                }
            }
        }
        return null;*/
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {

    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new SpotifyAudioTrackYT(trackInfo, youtubeAudioSourceManager);
    }

    @Override
    public void shutdown() {
        if(this.youtubeAudioSourceManager != null)
            this.youtubeAudioSourceManager.shutdown();
        if(this.service != null)
            this.service.shutdown();

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

    private AudioItem loadSearchResult(String query, int offset, int rawLimit) {
        int limit = Math.min(rawLimit, 200);

        try (HttpInterface httpInterface = manager.getHttpInterface()) {
            return withClientIdRetry(httpInterface,
                    (response) -> loadSearchResultsFromResponse(response, query),
                    () -> buildSearchUri(query, offset, limit)
            );
        } catch (IOException e) {
            throw new FriendlyException("Loading search results from SoundCloud failed.", SUSPICIOUS, e);
        }
    }

    private AudioItem loadSearchResultsFromResponse(HttpResponse response, String query) throws IOException {
        try {
            JsonBrowser searchResults = JsonBrowser.parse(response.getEntity().getContent());
            return extractTracksFromSearchResults(query, searchResults);
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
        }
    }

    private AudioItem extractTracksFromSearchResults(String query, JsonBrowser searchResults) {
        List<AudioTrack> tracks = new ArrayList<>();

        for (JsonBrowser item : searchResults.get("collection").values()) {
            if (!item.isNull()) {
                tracks.add(buildAudioTrack(item, null));
            }
        }

        return new BasicAudioPlaylist("Search results for: " + query, tracks, null, true);
    }

    private AudioTrack buildAudioTrack(JsonBrowser trackInfoJson, String secretToken) {
        String trackId = trackInfoJson.get("id").text();

        AudioTrackInfo trackInfo = new AudioTrackInfo(
                trackInfoJson.get("title").text(),
                trackInfoJson.get("user").get("username").text(),
                trackInfoJson.get("duration").as(Integer.class),
                secretToken != null ? trackId + "|" + secretToken : trackId,
                false,
                trackInfoJson.get("permalink_url").text()
        );

        return new SoundCloudAudioTrack(trackInfo, manager);
    }

    private URI buildSearchUri(String query, int offset, int limit) {
        try {
            return new URIBuilder("https://api-v2.soundcloud.com/search/tracks")
                    .addParameter("q", query)
                    .addParameter("client_id", manager.getClientId())
                    .addParameter("offset", String.valueOf(offset))
                    .addParameter("limit", String.valueOf(limit))
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T withClientIdRetry(HttpInterface httpInterface, ResponseHandler<T> handler, URIProvider uriProvider) throws IOException {
        try {
            HttpResponse response = httpInterface.execute(new HttpGet(uriProvider.provide()));
            int statusCode = response.getStatusLine().getStatusCode();

            try {
                if (statusCode != 401) {
                    return handler.handle(response);
                }
            } finally {
                EntityUtils.consumeQuietly(response.getEntity());
            }

            manager.updateClientId();
            response = httpInterface.execute(new HttpGet(uriProvider.provide()));

            try {
                return handler.handle(response);
            } finally {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private interface ResponseHandler<T> {
        T handle(HttpResponse response) throws IOException;
    }

    private interface URIProvider {
        URI provide() throws URISyntaxException;
    }
}
