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

package ml.duncte123.skybot.audio.sourcemanagers.youtube;

import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchResultLoader;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.http.ExtendedHttpConfigurable;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ml.duncte123.skybot.audio.sourcemanagers.youtube.YoutubeAudioSourceManagerOverride.DoNotCache;
import net.notfab.caching.client.CacheClient;
import net.notfab.caching.shared.SearchParams;
import net.notfab.caching.shared.YoutubeTrack;

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
        final AudioItem cacheItem = this.searchCache(query, trackFactory);

        if (cacheItem == null) {
            return this.searchYoutubeAPI(query, trackFactory);
        }

        return cacheItem;
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

            final AudioTrackInfo info = videoToTrackInfo(
                getVideoById(searchResults.get(0).getId().getVideoId(), this.apiKey)
            );

            return trackFactory.apply(info);
        }
        catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions(e);
        }
    }

    private AudioItem searchCache(String query, Function<AudioTrackInfo, AudioTrack> trackFactory) {
        final SearchParams searchParams = new SearchParams().setSearch(query);
        final List<YoutubeTrack> found = this.cacheClient.search(searchParams);

        if (found.isEmpty()) {
            return null;
        }

        final AudioTrackInfo info = found.get(0).toAudioTrack(null).getInfo();
        final AudioTrack audioTrack = trackFactory.apply(info);

        return new DoNotCache(audioTrack);
    }
}
