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

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.*;

import java.lang.reflect.Field;
import java.util.List;

public class SpotifyAudioTrack extends YoutubeAudioTrack {
    private final SpotifyAudioSourceManager sourceManager;

    private String youtubeId;

    /* default */ SpotifyAudioTrack(AudioTrackInfo trackInfo, SpotifyAudioSourceManager sourceManager) {
        super(trackInfo, sourceManager.youtubeAudioSourceManager);
        this.sourceManager = sourceManager;
    }

    // custom override to load the youtube id
    @Override
    public String getIdentifier() {
        if (this.youtubeId == null) {
            final AudioTrackInfo info = this.trackInfo;
            final AudioItem audioItem = this.sourceManager.youtubeAudioSourceManager.loadItem(
                null,
                new AudioReference(
                    "ytsearch:" + info.title + ' ' + info.author,
                    null
                )
            );

            if (audioItem instanceof BasicAudioPlaylist results) {
                final List<AudioTrack> tracks = results.getTracks();

                if (tracks.isEmpty()) {
                    throw new FriendlyException("Failed to read info for " + info.uri, Severity.SUSPICIOUS, null);
                }

                final AudioTrack audioTrack = tracks.stream()
                    // TODO: margin of 3 seconds?
                    .filter((track) -> track.getDuration() == info.length)
                    .findFirst()
                    .orElseGet(() -> tracks.get(0));

                // Identifier === youtube video id
                this.youtubeId = audioTrack.getIdentifier();

                // HACK: set the identifier on the trackInfo object
                this.setIdentifier(this.youtubeId);
            } else {
                throw new FriendlyException("Search results were not a playlist", Severity.FAULT, null);
            }
        }

        return this.youtubeId;
    }

    // TODO: I could probably override the track info class with a custom one that has a setter for the identifier
    private void setIdentifier(String videoId) {
        final Class<AudioTrackInfo> infoCls = AudioTrackInfo.class;

        try {
            final Field identifier = infoCls.getDeclaredField("identifier");

            identifier.setAccessible(true);

            identifier.set(this.trackInfo, videoId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new FriendlyException("Failed to look up youtube track", Severity.SUSPICIOUS, e);
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new SpotifyAudioTrack(trackInfo, sourceManager);
    }
}
