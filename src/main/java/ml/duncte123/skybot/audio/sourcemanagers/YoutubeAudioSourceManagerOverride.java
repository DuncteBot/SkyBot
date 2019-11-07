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

package ml.duncte123.skybot.audio.sourcemanagers;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.notfab.caching.client.CacheClient;
import net.notfab.caching.shared.CacheResponse;

import java.io.IOException;

import static ml.duncte123.skybot.utils.YoutubeUtils.getVideoById;
import static ml.duncte123.skybot.utils.YoutubeUtils.videoToTrack;

public class YoutubeAudioSourceManagerOverride extends YoutubeAudioSourceManager {

    private final CacheClient cacheClient;
    private final String ytApiKey;

    public YoutubeAudioSourceManagerOverride(boolean allowSearch, CacheClient cacheClient, String ytApiKey) {
        super(allowSearch);
        this.cacheClient = cacheClient;
        this.ytApiKey = ytApiKey;
    }

    @Override
    public AudioItem loadTrackWithVideoId(String videoId, boolean mustExist) {
        final CacheResponse cacheResponse = this.cacheClient.get(videoId);

        if (!cacheResponse.failure && cacheResponse.getTrack() != null) {
            final AudioTrack track = cacheResponse.getTrack().toAudioTrack(this);
            return new DoNotCache(track);
        }

        if (mustExist) {
            return getFromYoutubeApi(videoId);
        }

        return super.loadTrackWithVideoId(videoId, mustExist);
    }

    private AudioItem getFromYoutubeApi(String videoId) {
        try {
            return videoToTrack(
                getVideoById(videoId, this.ytApiKey),
                this
            );
        } catch (IOException e) {
            throw new FriendlyException("This video does not exist", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    /**
     * Don't cache tracks that are already retrieved from the cache
     */
    public static class DoNotCache extends YoutubeAudioTrack {
        DoNotCache(AudioTrack track) {
            super(track.getInfo(), (YoutubeAudioSourceManager) track.getSourceManager());
        }
    }
}
