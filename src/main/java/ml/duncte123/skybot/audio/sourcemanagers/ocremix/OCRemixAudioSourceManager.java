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

package ml.duncte123.skybot.audio.sourcemanagers.ocremix;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.jsoup.nodes.Element;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OCRemixAudioSourceManager implements AudioSourceManager {
    private static final Pattern REMIX_PATTERN = Pattern.compile("https?://(?:www\\.)?ocremix\\.org/remix/(OCR[\\d]+)");

    @Override
    public String getSourceName() {
        return "ocremix";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        // https://ocremix.org/remix/OCR03310?view=xml
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        //
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return null;
    }

    @Override
    public void shutdown() {
        //
    }

    @Nullable
    private OCRTrackMeta extractTrackData(Element el) {
        // el == <view name="xml" render="0.0874">

        final Element remix = el.selectFirst("remix");

        if (remix == null) {
            return null;
        }

        final String remixers = el.selectFirst("remixers")
            .children()
            .stream()
            .map((remixer) -> remixer.attr("name"))
            .collect(Collectors.joining(", "));

        return new OCRTrackMeta(
            remix.attr("id"),
            remix.attr("name"),
            remix.attr("file_name"),
            Integer.parseInt(remix.attr("track_length")),
            remixers
        );
    }

    static record OCRTrackMeta(
        String id,
        String name,
        String fileName,
        int trackLength, // seconds

        String remixers
    ) {
        //
    }
}
