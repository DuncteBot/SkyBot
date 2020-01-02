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

package ml.duncte123.skybot.audio.sourcemanagers.youtube;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubePlaylistLoader;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import io.sentry.Sentry;
import ml.duncte123.skybot.objects.YoutubePlaylistMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static ml.duncte123.skybot.utils.YoutubeUtils.getPlaylistPageById;

public class YoutubeApiPlaylistLoader implements YoutubePlaylistLoader {
    private final String apiKey;
    private volatile int playlistPageCount = 6;

    public YoutubeApiPlaylistLoader(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void setPlaylistPageCount(int playlistPageCount) {
        this.playlistPageCount = playlistPageCount;
    }

    @Override
    public AudioPlaylist load(HttpInterface httpInterface, String playlistId, String selectedVideoId, Function<AudioTrackInfo, AudioTrack> trackFactory) {
        try {
            final YoutubePlaylistMetadata firstPage = getPlaylistPageById(playlistId, this.apiKey, null, true);

            if (firstPage == null) {
                throw new FriendlyException("This playlist does not exist", COMMON, null);
            }

            return buildPlaylist(firstPage, playlistId, selectedVideoId, trackFactory);
        }
        catch (IOException e) {
            Sentry.capture(e);

            throw ExceptionTools.wrapUnfriendlyExceptions(e);
        }
    }

    private AudioPlaylist buildPlaylist(YoutubePlaylistMetadata firstPage, String playlistId, String selectedVideoId,
                                        Function<AudioTrackInfo, AudioTrack> trackFactory) throws IOException {
        final List<AudioTrack> convertedTracks = new ArrayList<>();

        firstPage.getTracks()
            .stream()
            .map(trackFactory)
            .forEach(convertedTracks::add);

        String nextPageKey = firstPage.getNextPageKey();
        int loadCount = 0;
        final int pageCount = playlistPageCount;

        while (nextPageKey != null && ++loadCount < pageCount) {
            nextPageKey = fetchNextPage(nextPageKey, playlistId, trackFactory, convertedTracks);
        }

        return new BasicAudioPlaylist(
            firstPage.getTitle(),
            convertedTracks,
            getSelectedTrack(selectedVideoId, convertedTracks),
            false
        );
    }

    private AudioTrack getSelectedTrack(String selectedVideoId, List<AudioTrack> tracks) {
        if (selectedVideoId == null) {
            return null;
        }

        for (final AudioTrack track : tracks) {
            if (selectedVideoId.equals(track.getIdentifier())) {
                return track;
            }
        }

        return null;
    }

    private String fetchNextPage(String nextPageKey, String playlistId, Function<AudioTrackInfo, AudioTrack> trackFactory,
                                                  List<AudioTrack> tracks) throws IOException {
        final YoutubePlaylistMetadata nextPage = getPlaylistPageById(playlistId, this.apiKey, nextPageKey, false);

        if (nextPage == null) {
            return null;
        }

        nextPage.getTracks()
            .stream()
            .map(trackFactory)
            .forEach(tracks::add);

        return nextPage.getNextPageKey();
    }
}
