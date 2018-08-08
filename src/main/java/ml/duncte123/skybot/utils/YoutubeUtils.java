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

package ml.duncte123.skybot.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import ml.duncte123.skybot.Variables;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class YoutubeUtils {

    private static YouTube youtube;

    static {
        try {
            youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), (unused) -> {
            })
                    .setApplicationName("SkyBot-youtube-search")
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }


    public static Video getVideoById(String videoID, String apiKey) throws Exception {
        return youtube.videos().list("snippet,statistics,contentDetails")
                .setId(videoID)
                .setKey(apiKey)
                .execute()
                .getItems().get(0);
    }


    public static List<SearchResult> searchYoutube(String query, String apiKey) throws IOException {
        return youtube.search().list("id,snippet")
                .setKey(apiKey)
                .setQ(query)
                .setType("video")
                .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                .setMaxResults(1L)
                .execute()
                .getItems();
    }

}
