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

package ml.duncte123.skybot.audio.sourcemanagers.clypit;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.audio.sourcemanagers.IdentifiedAudioReference;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class ClypitAudioSourceManager extends HttpAudioSourceManager {

    private static final Pattern CLYPIT_REGEX = Pattern.compile("(http://|https://(www\\.)?)?clyp\\.it/(.*)");

    @Override
    public String getSourceName() {
        return "clypit";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        final Matcher m = CLYPIT_REGEX.matcher(reference.identifier);

        if (!m.matches()) {
            return null;
        }

        try {
            final String clypitId = m.group(m.groupCount());
            final JsonBrowser json = fetchJson(clypitId);

            if (json == null) {
                return AudioReference.NO_TRACK;
            }

            return new IdentifiedAudioReference(
                json.get("Mp3Url").safeText(),
                reference.identifier,
                json.get("Title").safeText()
            );
        }
        catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Something went wrong", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // empty because we don't need them
    }

    // Switched from WebUtils to lavaplayer's stuff because that is better I guess
    private JsonBrowser fetchJson(String itemId) throws IOException {
        final HttpGet httpGet = new HttpGet("https://api.clyp.it/" + itemId);

        try (final CloseableHttpResponse response = getHttpInterface().execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                if (statusCode == 404) {
                    return null;
                }

                throw new IOException("Unexpected status code for video page response: " + statusCode);
            }


            final String json = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

            return JsonBrowser.parse(json);
        }
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new ClypitAudioTrack(trackInfo, this);
    }
}
