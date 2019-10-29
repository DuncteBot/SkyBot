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

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import ml.duncte123.skybot.Author;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

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

    public static Video getVideoById(String videoID, String apiKey) throws Exception {
        return getVideosByIds(videoID, apiKey).get(0);
    }

    public static List<Video> getVideosByIds(String videoIds, String apiKey) throws IOException {
        return youtube.videos().list("id,snippet,contentDetails")
            .setId(videoIds)
            .setKey(apiKey)
            .setFields("items(id/*,snippet/title,snippet/channelTitle,contentDetails/duration)")
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

    public static String getThumbnail(Video video) {
        return getThumbnail(video.getId());
    }

    public static String getThumbnail(String videoID) {
        return "https://i.ytimg.com/vi/" + videoID + "/mqdefault.jpg";
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
