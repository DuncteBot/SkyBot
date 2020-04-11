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

package ml.duncte123.skybot.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.YoutubePlaylistMetadata;

import javax.annotation.Nullable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class YoutubeUtils {

    private static YouTube youtube;

    static {
        try {
            youtube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
                .setApplicationName("SkyBot-youtube-search")
                .build();
        }
        catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static Video getVideoById(String videoID, String apiKey) throws IOException {
        final List<Video> items = getVideosByIdBase(videoID, apiKey)
            .setMaxResults(1L)
            .execute()
            .getItems();

        return items.isEmpty() ? null : items.get(0);
    }

    public static List<Video> getVideosByIds(String videoIds, String apiKey) throws IOException {
        return getVideosByIdBase(videoIds, apiKey)
            .execute()
            .getItems();
    }

    public static List<SearchResult> searchYoutubeIdOnly(String query, String apiKey, long size) throws IOException {
        return youtube.search().list("id")
            .setKey(apiKey)
            .setQ(query)
            .setType("video")
            .setMaxResults(size)
            .execute()
            .getItems();
    }

    public static List<SearchResult> searchYoutube(String query, String apiKey, long size) throws IOException {
        return youtube.search().list("snippet")
            .setKey(apiKey)
            .setQ(query)
            .setType("video")
            .setFields("items(id/kind,id/videoId,snippet/title)")
            .setMaxResults(size)
            .execute()
            .getItems();
    }

    @Nullable
    public static YoutubePlaylistMetadata getPlaylistPageById(String playlistId, String apiKey, @Nullable String nextPageKey, boolean withExtraData) throws IOException {
        String title = "";

        if (withExtraData) {
            title = getPlayListName(playlistId, apiKey);
        }

        if (title == null) {
            return null;
        }

        final PlaylistItemListResponse playlistItems = youtube.playlistItems()
            .list("snippet,contentDetails")
            .setPageToken(nextPageKey)
            .setPlaylistId(playlistId)
            .setMaxResults(20L)
//            .setMaxResults(1L)
            .setKey(apiKey)
            .execute();

        final List<PlaylistItem> items = playlistItems.getItems();

        if (items.isEmpty()) {
            return new YoutubePlaylistMetadata(playlistId, title, null, new ArrayList<>());
        }

        final List<AudioTrackInfo> changedItems = items.stream()
            .map((playlistItem) -> playListItemToTrackInfo(playlistItem, apiKey))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return new YoutubePlaylistMetadata(playlistId, title, playlistItems.getNextPageToken(), changedItems);
    }

    public static String getThumbnail(Video video) {
        return getThumbnail(video.getId());
    }

    public static String getThumbnail(String videoID) {
        return "https://i.ytimg.com/vi/" + videoID + "/hq720.jpg";
    }

    @Nullable
    public static AudioTrackInfo videoToTrackInfo(@Nullable Video video) {
        if (video == null) {
            return null;
        }

        final VideoSnippet snippet = video.getSnippet();
        final VideoContentDetails details = video.getContentDetails();

        return new AudioTrackInfo(
            snippet.getTitle(),
            snippet.getChannelTitle(),
            Duration.parse(details.getDuration()).toMillis(),
            video.getId(),
            false,
            "https://www.youtube.com/watch?v=" + video.getId()
        );
    }

    public static AudioTrackInfo playListItemToTrackInfo(PlaylistItem playlistItem, String apiKey) {
        try {
            final String videoId = playlistItem.getContentDetails().getVideoId();

            if (videoId == null) {
                return null;
            }

            final Video video = getVideoById(videoId, apiKey);

            return videoToTrackInfo(video);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null; // Should never happen tbh
        }
    }

    public static YoutubeAudioTrack videoToTrack(Video video, YoutubeAudioSourceManager sourceManager) {
        return new YoutubeAudioTrack(videoToTrackInfo(video), sourceManager);
    }

    private static YouTube.Videos.List getVideosByIdBase(String videoIds, String apiKey) throws IOException {
        return youtube.videos().list("id,snippet,contentDetails")
            .setId(videoIds)
            .setKey(apiKey)
            .setFields("items(id/*,snippet/title,snippet/channelTitle,contentDetails/duration)");
    }

    /**
     * Gets the name for a playlist
     * <p>
     * IMPORTANT: returns null if the playlist does not exist
     */
    @Nullable
    private static String getPlayListName(String playlistId, String apiKey) throws IOException {
        final List<Playlist> playlists = youtube.playlists()
            .list("snippet")
            .setId(playlistId)
            .setKey(apiKey)
            .execute()
            .getItems();

        if (playlists.isEmpty()) {
            return null;
        }

        final Playlist playlist = playlists.get(0);

        return playlist.getSnippet().getTitle();
    }

    /*private static YoutubeTrack searchCache(String title, String author, CacheClient cacheClient) {
        final SearchParams params = new SearchParams()
            .setSearch(title + " " + author)
            .setTitle(title.split("\\s+"))
            .setAuthor(author.split("\\s+"));

        final List<YoutubeTrack> found = cacheClient.search(params);

        if (found.isEmpty()) {
            return null;
        }

        return found.get(0);
    }

    private static Video cacheToYoutubeVideo(YoutubeTrack track) {
        return new Video()
            .setId(track.getId())
            .setKind("youtube#video")
            .setSnippet(
                new VideoSnippet()
                    .setTitle(track.getTitle())
                    .setChannelTitle(track.getAuthor())
            )
            .setContentDetails(
                new VideoContentDetails()
                    .setDuration(
                        Duration.ofMillis(track.getLength()).toString()
                    )
            );
    }*/
}
