/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.objects.audiomanagers.clypit;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import ml.duncte123.skybot.utils.WebUtils;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;
import static com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools.getHeaderValue;

public class ClypitAudioSourceManager extends HttpAudioSourceManager {

    private static final Pattern CLYPIT_REGEX = Pattern.compile("(http://|https://(www\\.)?)?clyp\\.it/(.*)");

    @Override
    public String getSourceName() {
        return "clyp.it";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        Matcher m = CLYPIT_REGEX.matcher(reference.identifier);
        if (m.matches()) {
            try {
                String clypitId = m.group(m.groupCount());
                JSONObject json = WebUtils.ins.getJSONObject("https://api.clyp.it/" + clypitId).execute();
                AudioReference httpReference = getAsHttpReference(new AudioReference(json.getString("Mp3Url"), json.getString("Title")));
                return handleLoadResult(detectContainer(httpReference));
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    private AudioReference getAsHttpReference(AudioReference reference) {
        if (reference.identifier.startsWith("https://") || reference.identifier.startsWith("http://")) {
            return reference;
        } else if (reference.identifier.startsWith("icy://")) {
            return new AudioReference("http://" + reference.identifier.substring(6), reference.title);
        }
        return null;
    }

    private MediaContainerDetectionResult detectContainer(AudioReference reference) {
        MediaContainerDetectionResult result;

        try (HttpInterface httpInterface = getHttpInterface()) {
            result = detectContainerWithClient(httpInterface, reference);
        } catch (IOException e) {
            throw new FriendlyException("Connecting to the URL failed.", SUSPICIOUS, e);
        }

        return result;
    }

    private MediaContainerDetectionResult detectContainerWithClient(HttpInterface httpInterface, AudioReference reference) throws IOException {
        try (PersistentHttpStream inputStream = new PersistentHttpStream(httpInterface, new URI(reference.identifier), Long.MAX_VALUE)) {
            int statusCode = inputStream.checkStatusCode();
            String redirectUrl = HttpClientTools.getRedirectLocation(reference.identifier, inputStream.getCurrentResponse());

            if (redirectUrl != null) {
                return new MediaContainerDetectionResult(null, new AudioReference(redirectUrl, null));
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            } else if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new FriendlyException("That URL is not playable.", COMMON, new IllegalStateException("Status code " + statusCode));
            }

            MediaContainerHints hints = MediaContainerHints.from(getHeaderValue(inputStream.getCurrentResponse(), "Content-Type"), null);
            return MediaContainerDetection.detectContainer(reference, inputStream, hints);
        } catch (URISyntaxException e) {
            throw new FriendlyException("Not a valid URL.", COMMON, e);
        }
    }
}
