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

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OCRemixAudioSourceManager extends AbstractDuncteBotHttpSource {
    private static final Pattern REMIX_PATTERN = Pattern.compile("https?://(?:www\\.)?ocremix\\.org/remix/(?<id>OCR[\\d]+)(?:.*)?");

    @Override
    public String getSourceName() {
        return "ocremix";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final String identifier = reference.identifier;

        System.out.println(identifier);

        final Matcher matcher = REMIX_PATTERN.matcher(identifier);

        // TODO: check if starts with OCR?
        if (!matcher.matches()) {
            return null;
        }

        final String id = matcher.group("id");
        // https://ocremix.org/remix/OCR03310?view=xml
        final HttpGet httpGet = new HttpGet("https://ocremix.org/remix/"+id+"?view=xml");
        try (final CloseableHttpResponse response = getHttpInterface().execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new IOException("Unexpected status code for OCR page response: " + statusCode);
            }

            final String xml = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            final Document jsoup = Jsoup.parse(xml);
            final OCRTrackMeta meta = this.extractTrackData(jsoup);

            if (meta == null) {
                return null;
            }

            final AudioTrackInfo info = new AudioTrackInfo(
                meta.name,
                meta.remixers,
                meta.trackLength,
                meta.id,
                false,
                meta.fileName
            );

            return new OCRemixAudioTrack(info, this);
        } catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Something went wrong", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new OCRemixAudioTrack(trackInfo, this);
    }

    @Nullable
    private OCRTrackMeta extractTrackData(Element elem) {
        final Element remix = elem.selectFirst("remix");

        if (remix == null) {
            return null;
        }

        final String remixers = elem.selectFirst("remixers")
            .children()
            .stream()
            .map((remixer) -> remixer.attr("name"))
            .collect(Collectors.joining(", "));

        return new OCRTrackMeta(
            remix.attr("id"),
            remix.attr("name"),
            remix.attr("file_name"),
            Long.parseLong(remix.attr("track_length")) * 1000, // convert from seconds
            remixers
        );
    }

    record OCRTrackMeta(
        String id,
        String name,
        String fileName,
        long trackLength, // seconds

        String remixers
    ) {
        //
    }
}
