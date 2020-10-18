/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.audio.sourcemanagers.spotify;

import com.dunctebot.sourcemanagers.AudioTrackInfoWithImage;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.IPlaylistItem;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.audio.BigChungusPlaylist;
import ml.duncte123.skybot.audio.TrackScheduler;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ml.duncte123.skybot.utils.YoutubeUtils.*;

@Author(nickname = "duncte123", author = "Duncan Sterken")
@SuppressWarnings("PMD.NullAssignment")
public class SpotifyAudioSourceManager implements AudioSourceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyAudioSourceManager.class);

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
            LOGGER.error("Could not load Spotify keys");
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
//            final List<String> videoIDs = new ArrayList<>();
            final NavigableSet<String> videoIDs = new TreeSet<>(Comparator.reverseOrder());
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

        try {final Playlist spotifyPlaylist = this.spotifyApi.getPlaylist(playListId).build().execute();
            List<PlaylistTrack> playlistTracks = List.of(spotifyPlaylist.getTracks().getItems());

            if (playlistTracks.isEmpty()) {
                return null;
            }

            final int originalSize = playlistTracks.size();

            if (originalSize > TrackScheduler.MAX_QUEUE_SIZE && !isPatron) {
                // yes this is correct, last param is exclusive so it ranges from 0-49
                playlistTracks = playlistTracks.subList(0, TrackScheduler.MAX_QUEUE_SIZE);
            }

//            final List<String> videoIDs = new ArrayList<>();
            final NavigableSet<String> videoIDs = new TreeSet<>(Comparator.reverseOrder());

            for (final PlaylistTrack playlistTrack : playlistTracks) {
                if (playlistTrack.getIsLocal()) {
                    continue;
                }

                final IPlaylistItem item = playlistTrack.getTrack();

                if (!(item instanceof Track)) {
                    continue;
                }

                final Track track = (Track) item;
                final String videoId = searchYoutube(track.getName(), track.getArtists()[0].getName());

                if (videoId != null) {
                    videoIDs.add(videoId);
                }
            }

            final List<AudioTrack> finalPlaylist = getTrackListFromVideoIds(videoIDs, spotifyPlaylist.getImages());

            return new BigChungusPlaylist(spotifyPlaylist.getName(), finalPlaylist, finalPlaylist.get(0), false, originalSize);
        }
        catch (IllegalArgumentException ex) {
            throw new FriendlyException("This playlist could not be loaded, make sure that it's public", Severity.COMMON, ex);
        }
        catch (IOException | ParseException | SpotifyWebApiException e) {
            //logger.error("Something went wrong!", e);
            throw ExceptionTools.wrapUnfriendlyExceptions(e.getMessage(), Severity.FAULT, e);
        }
        /*catch (LimitReachedException e) {
            throw e;
        }
        catch (Exception e) {
            //logger.error("Something went wrong!", e);
            throw ExceptionTools.wrapUnfriendlyExceptions(e.getMessage(), Severity.FAULT, e);
        }*/

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

            final Video video = getVideoById(videoId, this.config.googl);

            if (video == null) {
                return null;
            }

            return audioTrackFromVideo(video, track.getAlbum().getImages());
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

            LOGGER.debug("Successfully retrieved an access token! " + clientCredentials.getAccessToken());
            LOGGER.debug("The access token expires in " + clientCredentials.getExpiresIn() + " seconds");
        }
        catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.error("Error while fetching Spotify token", e);

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

    private List<AudioTrack> getTrackListFromVideoIds(NavigableSet<String> videoIds, Image[] images) throws IOException {
        final List<AudioTrack> playList = new ArrayList<>();

        // the old way (only works for 50 trakcks, thanks youtube)
        // final String videoIdsJoined = String.join(",", videoIds);
        // final List<Video> videosByIds = getVideosByIds(videoIdsJoined, this.config.googl);
        // videosByIds.forEach((video) -> playList.add(audioTrackFromVideo(video, images)));

        // prevent creation of all the other lists here
        if (videoIds.isEmpty()) {
            return playList;
        }

        // 50 is the limit from youtube (this is not documented tho)
        final List<String> searchBatch = new ArrayList<>(50);

        while (!videoIds.isEmpty()) {
            searchBatch.clear();

            for (int i = 0; i < 50 && !videoIds.isEmpty(); i++) {
                searchBatch.add(videoIds.pollLast());
            }

            final String videoIdsJoined = String.join(",", searchBatch);
            final List<Video> videosByIds = getVideosByIds(videoIdsJoined, this.config.googl);

            videosByIds.forEach((video) -> playList.add(audioTrackFromVideo(video, images)));
        }

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

    private AudioTrack audioTrackFromVideo(Video video, Image[] images) {
        return new SpotifyAudioTrack(new AudioTrackInfoWithImage(
            video.getSnippet().getTitle(),
            video.getSnippet().getChannelTitle(),
            toLongDuration(video.getContentDetails().getDuration()),
            video.getId(),
            false,
            "https://youtube.com/watch?v=" + video.getId(),
            imageUrlOrThumbnail(images, video)
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
