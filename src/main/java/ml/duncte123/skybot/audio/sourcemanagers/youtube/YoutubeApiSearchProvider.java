/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.audio.sourcemanagers.youtube;

import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchResultLoader;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.http.ExtendedHttpConfigurable;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoBuilder;
import ml.duncte123.skybot.audio.sourcemanagers.youtube.YoutubeAudioSourceManagerOverride.DoNotCache;
import net.notfab.caching.client.CacheClient;
import net.notfab.caching.shared.CacheResponse;
import net.notfab.caching.shared.Track;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static ml.duncte123.skybot.utils.YoutubeUtils.*;

public class YoutubeApiSearchProvider implements YoutubeSearchResultLoader {
    private final String apiKey;
    private final CacheClient cacheClient;

    public YoutubeApiSearchProvider(String apiKey, CacheClient cacheClient) {
        this.apiKey = apiKey;
        this.cacheClient = cacheClient;
    }

    @Override
    public AudioItem loadSearchResult(String query, Function<AudioTrackInfo, AudioTrack> trackFactory) {
        return this.searchYoutubeAPI(query, trackFactory);
    }

    @Override
    public ExtendedHttpConfigurable getHttpConfiguration() {
        return null;
    }

    private AudioItem searchYoutubeAPI(String query, Function<AudioTrackInfo, AudioTrack> trackFactory) {
        try {
            final List<SearchResult> searchResults = searchYoutube(query, this.apiKey, 1L);

            if (searchResults.isEmpty()) {
                return null;
            }

            final String videoId = searchResults.get(0).getId().getVideoId();
            final Track fromCache = this.getTrackFromCache(videoId);

            if (fromCache != null) {
                final AudioTrackInfo info = AudioTrackInfoBuilder.empty()
                    .setTitle(fromCache.getTitle())
                    .setAuthor(fromCache.getAuthor())
                    .setLength(fromCache.getLength())
                    .setIdentifier(fromCache.getId())
                    .setIsStream(fromCache.isStream())
                    .setUri(fromCache.toURL())
                    .build();

                return new DoNotCache(trackFactory.apply(info));
            }

            final AudioTrackInfo info = videoToTrackInfo(
                getVideoById(searchResults.get(0).getId().getVideoId(), this.apiKey)
            );

            return trackFactory.apply(info);
        }
        catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions(e);
        }
    }

    private Track getTrackFromCache(String videoId) {
        final CacheResponse found = this.cacheClient.get(videoId);

        if (found.isFailure()) {
            return null;
        }

        return found.getTrack();
    }
}
