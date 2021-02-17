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
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
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
import ml.duncte123.skybot.audio.BigChungusPlaylist;
import ml.duncte123.skybot.audio.TrackScheduler;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern SPOTIFY_SECOND_PLAYLIST_REGEX = Pattern.compile("^(?:spotify)(?::user:(?:.*))?(?::playlist:)(.*)$");
    private final SpotifyApi spotifyApi;
    /* package */ final YoutubeAudioSourceManager youtubeAudioSourceManager;
    private final ScheduledExecutorService service;
    private final DunctebotConfig.Apis config;

    public SpotifyAudioSourceManager(YoutubeAudioSourceManager youtubeAudioSourceManager, DunctebotConfig.Apis config) {
        this.config = config;

        final String clientId = config.spotify.clientId;
        final String clientSecret = config.spotify.clientSecret;

        this.youtubeAudioSourceManager = youtubeAudioSourceManager;
        this.spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();

        this.service = Executors.newScheduledThreadPool(2, (r) -> new Thread(r, "Spotify-Token-Update-Thread"));
        service.scheduleAtFixedRate(this::updateAccessToken, 0, 1, TimeUnit.HOURS);
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
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
            final List<AudioTrack> playList = new ArrayList<>();
            final Future<Album> albumFuture = this.spotifyApi.getAlbum(res.group(res.groupCount())).build().executeAsync();
            final Album album = albumFuture.get();
            final Image[] images = album.getImages();

            for (final TrackSimplified t : album.getTracks().getItems()) {
                playList.add(buildTrackFromSimple(t, images));
            }

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

            final List<AudioTrack> finalPlaylist = new ArrayList<>();

            for (final PlaylistTrack playlistTrack : playlistTracks) {
                if (playlistTrack.getIsLocal()) {
                    continue;
                }

                final IPlaylistItem item = playlistTrack.getTrack();

                // playlist item can either be a track or podcast episode
                if (!(item instanceof Track)) {
                    continue;
                }

                final Track track = (Track) item;

                finalPlaylist.add(buildTrack(track));
            }

            if (finalPlaylist.isEmpty()) {
                throw new FriendlyException("This playlist does not contain playable tracks (podcasts cannot be played)", Severity.COMMON, null);
            }

            return new BigChungusPlaylist(spotifyPlaylist.getName(), finalPlaylist, finalPlaylist.get(0), false, originalSize);
        }
        catch (IllegalArgumentException ex) {
            throw new FriendlyException("This playlist could not be loaded, make sure that it's public", Severity.COMMON, ex);
        }
        catch (IOException | ParseException | SpotifyWebApiException e) {
            //logger.error("Something went wrong!", e);
            throw ExceptionTools.wrapUnfriendlyExceptions(e.getMessage(), Severity.FAULT, e);
        }
    }

    private AudioItem getSpotifyTrack(AudioReference reference) {

        final Matcher res = SPOTIFY_TRACK_REGEX.matcher(reference.identifier);

        if (!res.matches()) {
            return null;
        }

        try {
            final Track track = this.spotifyApi.getTrack(res.group(res.groupCount())).build().execute();

            return buildTrack(track);
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
        return new SpotifyAudioTrack(trackInfo, this.config.googl, this);
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

    private AudioTrack buildTrackFromSimple(TrackSimplified track, Image[] images) {
        return new SpotifyAudioTrack(
            new AudioTrackInfoWithImage(
                track.getName(),
                track.getArtists()[0].getName(),
                track.getDurationMs(),
                track.getId(),
                false,
                track.getExternalUrls().get("spotify"),
                getImageOrDefault(images)
            ),
            this.config.googl,
            this
        );
    }

    private AudioTrack buildTrack(Track track) {
        return new SpotifyAudioTrack(
            new AudioTrackInfoWithImage(
                track.getName(),
                track.getArtists()[0].getName(),
                track.getDurationMs(),
                track.getId(),
                false,
                track.getExternalUrls().get("spotify"),
                getImageOrDefault(track.getAlbum().getImages())
            ),
            this.config.googl,
            this
        );
    }

    private String getImageOrDefault(Image[] images) {
        if (images.length > 0) {
            return images[0].getUrl();
        }

        return "https://dunctebot.com/img/favicon.png";
    }
}
