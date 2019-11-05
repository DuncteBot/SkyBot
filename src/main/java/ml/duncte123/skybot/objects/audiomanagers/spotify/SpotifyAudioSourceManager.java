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

package ml.duncte123.skybot.objects.audiomanagers.spotify;

import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.audio.TrackScheduler;
import ml.duncte123.skybot.exceptions.LimitReachedException;
import ml.duncte123.skybot.objects.audiomanagers.AudioTrackInfoWithImage;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ml.duncte123.skybot.utils.YoutubeUtils.*;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SpotifyAudioSourceManager implements AudioSourceManager {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyAudioSourceManager.class);

    private static final String PROTOCOL_REGEX = "?:spotify:(track:)|(?:http://|https://)[a-z]+\\.";
    private static final String DOMAIN_REGEX = "spotify\\.com/";
    private static final String TRACK_REGEX = "track/([a-zA-z0-9]+)";
    private static final String ALBUM_REGEX = "album/([a-zA-z0-9]+)";
    private static final String USER_PART = "user/(?:.*)/";
    private static final String PLAYLIST_REGEX = "playlist/([a-zA-z0-9]+)";
    private static final String REST_REGEX = "(?:.*)";
    private static final String SPOTIFY_BASE_REGEX = PROTOCOL_REGEX + DOMAIN_REGEX;

    private static final Pattern SPOTIFY_TRACK_REGEX = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + TRACK_REGEX + ")" + REST_REGEX + "$");
    private static final Pattern SPOTIFY_ALBUM_REGEX = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + ALBUM_REGEX + ")" + REST_REGEX + "$");
    private static final Pattern SPOTIFY_PLAYLIST_REGEX = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + ")" + PLAYLIST_REGEX + REST_REGEX + "$");
    private static final Pattern SPOTIFY_PLAYLIST_REGEX_USER = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + ")" +
        USER_PART + PLAYLIST_REGEX + REST_REGEX + "$");
    private static final Pattern SPOTIFY_SECOND_PLAYLIST_REGEX = Pattern.compile("^(?:spotify:user:)(?:.*)(?::playlist:)(.*)$");
    private final SpotifyApi spotifyApi;
    private final YoutubeAudioSourceManager youtubeAudioSourceManager;
    private final ScheduledExecutorService service;
    private final DunctebotConfig.Apis config;

    public SpotifyAudioSourceManager(YoutubeAudioSourceManager youtubeAudioSourceManager, DunctebotConfig.Apis config) {
        this.config = config;

        final String clientId = config.spotify.clientId;
        final String clientSecret = config.spotify.clientSecret;
        final String youtubeApiKey = config.googl;

        if (clientId == null || clientSecret == null || youtubeApiKey == null) {
            logger.error("Could not load Spotify keys");
            this.spotifyApi = null;
            this.service = null;
            this.youtubeAudioSourceManager = null;
        } else {
            this.youtubeAudioSourceManager = youtubeAudioSourceManager;
            this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();

            this.service = Executors.newScheduledThreadPool(2, (r) -> new Thread(r, "Spotify-Token-Update-Thread"));
            service.scheduleAtFixedRate(this::updateAccessToken, 0, 1, TimeUnit.HOURS);
        }
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        return loadItem(reference, false);
    }

    public AudioItem loadItem(AudioReference reference, boolean isPatron) {

        AudioItem item = getSpotifyAlbum(reference);

        if (item == null) {
            item = getSpotifyPlaylist(reference, isPatron);
        }

        if (item == null) {
            item = getSpotifyTrack(reference);
        }

        return item;
    }

    private AudioItem getSpotifyAlbum(AudioReference reference) {
        final Matcher res = SPOTIFY_ALBUM_REGEX.matcher(reference.identifier);

        if (!res.matches()) {
            return null;
        }

        try {
            final List<String> videoIDs = new ArrayList<>();
            final Future<Album> albumFuture = this.spotifyApi.getAlbum(res.group(res.groupCount())).build().executeAsync();
            final Album album = albumFuture.get();

            for (final TrackSimplified t : album.getTracks().getItems()) {
                final String videoId = searchYoutube(t.getName(), album.getArtists()[0].getName());

                if (videoId != null) {
                    videoIDs.add(videoId);
                }
            }

            final List<AudioTrack> playList = getTrackListFromVideoIds(videoIDs, album.getImages());

            return new BasicAudioPlaylist(album.getName(), playList, playList.get(0), false);
        }
        catch (Exception e) {
            //logger.error("Something went wrong!", e);
            throw new FriendlyException(e.getMessage(), Severity.FAULT, e);
        }
    }

    private AudioItem getSpotifyPlaylist(AudioReference reference, boolean isPatron) {

        final Matcher res = getSpotifyPlaylistFromString(reference.identifier);

        if (!res.matches()) {
            return null;
        }

        final String playListId = res.group(res.groupCount());

        try {
            final List<String> videoIDs = new ArrayList<>();

            final Playlist spotifyPlaylist = this.spotifyApi.getPlaylist(playListId).build().execute();
            final PlaylistTrack[] playlistTracks = spotifyPlaylist.getTracks().getItems();

            if (playlistTracks.length == 0) {
                return null;
            }

            if (playlistTracks.length > TrackScheduler.QUEUE_SIZE && !isPatron) {
                throw new LimitReachedException("The playlist is too big", TrackScheduler.QUEUE_SIZE);
            }

            for (final PlaylistTrack playlistTrack : playlistTracks) {
                if (playlistTrack.getIsLocal()) {
                    continue;
                }

                final Track track = playlistTrack.getTrack();
                final String videoId = searchYoutube(track.getName(), track.getArtists()[0].getName());

                if (videoId != null) {
                    videoIDs.add(videoId);
                }
            }

            final List<AudioTrack> finalPlaylist = getTrackListFromVideoIds(videoIDs, spotifyPlaylist.getImages());

            return new BasicAudioPlaylist(spotifyPlaylist.getName(), finalPlaylist, finalPlaylist.get(0), false);
        }
        catch (IllegalArgumentException ex) {
            throw new FriendlyException("This playlist could not be loaded, make sure that it's public", Severity.COMMON, ex);
        }
        catch (LimitReachedException e) {
            throw e;
        }
        catch (Exception e) {
            //logger.error("Something went wrong!", e);
            throw new FriendlyException(e.getMessage(), Severity.FAULT, e);
        }

    }

    private AudioItem getSpotifyTrack(AudioReference reference) {

        final Matcher res = SPOTIFY_TRACK_REGEX.matcher(reference.identifier);

        if (!res.matches()) {
            return null;
        }

        try {
            final Track track = this.spotifyApi.getTrack(res.group(res.groupCount())).build().execute();
            final String videoId = searchYoutube(track.getName(), track.getArtists()[0].getName());

            if (videoId == null) {
                return null;
            }

            final Video v = getVideoById(videoId, this.config.googl);

            return audioTrackFromVideo(v, track.getAlbum().getImages());
        }
        catch (Exception e) {
            //logger.error("Something went wrong!", e);
            throw new FriendlyException(e.getMessage(), Severity.FAULT, e);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // Nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new SpotifyAudioTrack(trackInfo, this.youtubeAudioSourceManager);
    }

    @Override
    public void shutdown() {
        if (this.service != null) {
            this.service.shutdown();
        }

    }

    private void updateAccessToken() {
        try {
            final ClientCredentialsRequest request = this.spotifyApi.clientCredentials().build();
            final ClientCredentials clientCredentials = request.execute();

            // Set access token for further "spotifyApi" object usage
            this.spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            logger.debug("Successfully retrieved an access token! " + clientCredentials.getAccessToken());
            logger.debug("The access token expires in " + clientCredentials.getExpiresIn() + " seconds");
        }
        catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
            logger.error("Error while fetching Spotify token", e);

            // Retry after 10 seconds
            this.service.schedule(this::updateAccessToken, 10L, TimeUnit.SECONDS);
        }
    }

    private Matcher getSpotifyPlaylistFromString(String input) {
        final Matcher match = SPOTIFY_PLAYLIST_REGEX.matcher(input);

        if (match.matches()) {
            return match;
        }

        final Matcher withUser = SPOTIFY_PLAYLIST_REGEX_USER.matcher(input);

        if (withUser.matches()) {
            return withUser;
        }

        return SPOTIFY_SECOND_PLAYLIST_REGEX.matcher(input);
    }

    private List<AudioTrack> getTrackListFromVideoIds(List<String> videoIds, Image[] images) throws Exception {
        final String videoIdsJoined = String.join(",", videoIds);
        final List<Video> videosByIds = getVideosByIds(videoIdsJoined, this.config.googl);
        final List<AudioTrack> playList = new ArrayList<>();

        videosByIds.forEach((video) -> playList.add(audioTrackFromVideo(video, images)));

        return playList;
    }

    @Nullable
    private String searchYoutube(String title, String author) throws IOException {
        final List<SearchResult> results = searchYoutubeIdOnly(title + " " + author, this.config.googl, 1L);

        if (!results.isEmpty()) {
            return results.get(0).getId().getVideoId();
        }

        return null;
    }

    private AudioTrack audioTrackFromVideo(Video v, Image[] images) {
        return new SpotifyAudioTrack(new AudioTrackInfoWithImage(
            v.getSnippet().getTitle(),
            v.getSnippet().getChannelTitle(),
            toLongDuration(v.getContentDetails().getDuration()),
            v.getId(),
            false,
            "https://youtube.com/watch?v=" + v.getId(),
            imageUrlOrThumbnail(images, v)
        ), this.youtubeAudioSourceManager);
    }

    private String imageUrlOrThumbnail(Image[] images, Video video) {
        if (images.length > 0) {
            return images[0].getUrl();
        }

        return getThumbnail(video);
    }

    private long toLongDuration(String dur) {
        return Duration.parse(dur).toMillis();
    }
}
