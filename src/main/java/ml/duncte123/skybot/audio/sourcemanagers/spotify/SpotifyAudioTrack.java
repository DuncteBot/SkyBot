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

package ml.duncte123.skybot.audio.sourcemanagers.spotify;

import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static ml.duncte123.skybot.utils.YoutubeUtils.searchYoutubeIdOnly;

public class SpotifyAudioTrack extends YoutubeAudioTrack {
    private final String apiKey;
    private final SpotifyAudioSourceManager sourceManager;

    private String youtubeId;

    /* default */ SpotifyAudioTrack(AudioTrackInfo trackInfo, String apiKey, SpotifyAudioSourceManager sourceManager) {
        super(trackInfo, sourceManager.youtubeAudioSourceManager);
        this.apiKey = apiKey;
        this.sourceManager = sourceManager;
    }

    // custom override to load the youtube id
    @Override
    public String getIdentifier() {
        if (this.youtubeId == null) {
            final AudioTrackInfo info = this.trackInfo;
            try {
                final List<SearchResult> results = searchYoutubeIdOnly(info.title + " " + info.author, this.apiKey, 1L);

                if (results.isEmpty()) {
                    throw new FriendlyException("Failed to read info for " + info.uri, Severity.SUSPICIOUS, null);
                }

                this.youtubeId = results.get(0).getId().getVideoId();
                // HACK: set the identifier on the trackInfo object
                this.setIdentifier(this.youtubeId);
            } catch (IOException e) {
               throw new FriendlyException("Failed to look up youtube track", Severity.SUSPICIOUS, e);
            }
        }

        return this.youtubeId;
    }

    private void setIdentifier(String videoId) {
        final Class<AudioTrackInfo> infoCls = AudioTrackInfo.class;

        try {
            final Field identifier = infoCls.getDeclaredField("identifier");

            identifier.setAccessible(true);

            identifier.set(this.trackInfo, videoId);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new FriendlyException("Failed to look up youtube track", Severity.SUSPICIOUS, e);
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new SpotifyAudioTrack(trackInfo, this.apiKey, sourceManager);
    }
}
