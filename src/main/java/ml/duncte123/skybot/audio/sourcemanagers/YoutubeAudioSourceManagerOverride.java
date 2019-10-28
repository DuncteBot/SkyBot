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
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import ml.duncte123.skybot.Variables;
import net.notfab.caching.shared.CacheResponse;

public class YoutubeAudioSourceManagerOverride extends YoutubeAudioSourceManager {

    private final Variables variables;

    public YoutubeAudioSourceManagerOverride(boolean allowSearch, Variables variables) {
        super(allowSearch);
        this.variables = variables;
    }

    @Override
    public AudioItem loadTrackWithVideoId(String videoId, boolean mustExist) {
        final CacheResponse cacheResponse = this.variables.getYoutubeCache().get(videoId);

        System.out.println(cacheResponse.toString());

        if (!cacheResponse.failure && cacheResponse.getTrack() != null) {
            return cacheResponse.getTrack().toAudioTrack(this);
        }

        return super.loadTrackWithVideoId(videoId, mustExist);
    }
}
