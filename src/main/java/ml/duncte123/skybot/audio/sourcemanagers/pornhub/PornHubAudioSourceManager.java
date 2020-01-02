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

package ml.duncte123.skybot.audio.sourcemanagers.pornhub;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.http.HttpContextFilter;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ml.duncte123.skybot.audio.sourcemanagers.AudioTrackInfoWithImage;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PornHubAudioSourceManager implements AudioSourceManager, HttpConfigurable {
    private static final Pattern VIDEO_REGEX = Pattern.compile("^https?://(www\\.)?pornhub\\.(com|net)/view_video\\.php\\?viewkey=([a-zA-Z0-9]+)(?:.*)$");
    private static final Pattern VIDEO_INFO_REGEX = Pattern.compile("var flashvars_\\d+ = (\\{.+})");
    private static final Pattern MODEL_INFO_REGEX = Pattern.compile("var MODEL_PROFILE = (\\{.+})");
    private final HttpInterfaceManager httpInterfaceManager;

    public PornHubAudioSourceManager() {
        httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();

        httpInterfaceManager.setHttpContextFilter(new FuckCookies());
    }

    @Override
    public String getSourceName() {
        return "pornhub";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        if (!VIDEO_REGEX.matcher(reference.identifier).matches()) {
            return null;
        }

        try {
            return loadItemOnce(reference);
        }
        catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Something went wrong", Severity.SUSPICIOUS, e);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // Nothing
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new PornHubAudioTrack(trackInfo, this);
    }

    @Override
    public void shutdown() {
        ExceptionTools.closeWithWarnings(httpInterfaceManager);
    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        httpInterfaceManager.configureRequests(configurator);
    }

    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        httpInterfaceManager.configureBuilder(configurator);
    }

    public HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }

    private AudioItem loadItemOnce(AudioReference reference) throws IOException {
        final String html = loadHtml(reference.identifier);

        if (html == null) {
            notAvailable();
        }

        final JsonBrowser videoInfo = getVideoInfo(html);
        final JsonBrowser modelInfo = getModelInfo(html);

        if (videoInfo == null || modelInfo == null) {
            notAvailable();
        }

        if ("true".equals(videoInfo.get("video_unavailable").safeText())) {
            notAvailable();
        }

        final String title = videoInfo.get("video_title").safeText();
        final String author = modelInfo.get("username").safeText();
        final int duration = Integer.parseInt(videoInfo.get("video_duration").safeText()) * 1000; // PornHub returns seconds
//        final Matcher matcher = VIDEO_REGEX.matcher(reference.identifier);
        final String identifier = /*matcher.matches() ? matcher.group(1) :*/ reference.identifier;
        final String uri = reference.identifier;
        final String imagUrl = videoInfo.get("image_url").safeText();

        return buildAudioTrack(
            title,
            author,
            duration,
            identifier,
            uri,
            imagUrl
        );
    }

    private PornHubAudioTrack buildAudioTrack(String title, String author, long duration, String identifier, String uri, String imageUrl) {
        return new PornHubAudioTrack(
            new AudioTrackInfoWithImage(
                title,
                author,
                duration,
                identifier,
                false,
                uri,
                imageUrl
            ),
            this
        );
    }

    private JsonBrowser getVideoInfo(String html) throws IOException {
        final Matcher matcher = VIDEO_INFO_REGEX.matcher(html);

        if (matcher.find()) {
            return JsonBrowser.parse(matcher.group(1));
        }

        return null;
    }

    private JsonBrowser getModelInfo(String html) throws IOException {
        final Matcher matcher = MODEL_INFO_REGEX.matcher(html);

        if (matcher.find()) {
            return JsonBrowser.parse(matcher.group(1));
        }

        return null;
    }

    private String loadHtml(String url) throws IOException {
        final HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("Cookie", "platform=pc");

        try (final CloseableHttpResponse response = getHttpInterface().execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                if (statusCode == 404) {
                    return null;
                }

                throw new IOException("Unexpected status code for video page response: " + statusCode);
            }

            return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        }
    }

    private void notAvailable() {
        throw new FriendlyException("This video is not available", Severity.COMMON, null);
    }

    private static class FuckCookies implements HttpContextFilter {
        @Override
        public void onContextOpen(HttpClientContext context) {
            CookieStore cookieStore = context.getCookieStore();

            if (cookieStore == null) {
                cookieStore = new BasicCookieStore();
                context.setCookieStore(cookieStore);
            }

            // Reset cookies for each sequence of requests.
            cookieStore.clear();
        }

        @Override
        public void onContextClose(HttpClientContext context) {
            // Not used
        }

        @Override
        public void onRequest(HttpClientContext context, HttpUriRequest request, boolean isRepetition) {
            // Not used
        }

        @Override
        public boolean onRequestResponse(HttpClientContext context, HttpUriRequest request, HttpResponse response) {
            return false;
        }

        @Override
        public boolean onRequestException(HttpClientContext context, HttpUriRequest request, Throwable error) {
            return false;
        }
    }
}
