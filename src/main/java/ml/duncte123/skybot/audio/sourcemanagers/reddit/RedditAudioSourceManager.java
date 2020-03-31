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

package ml.duncte123.skybot.audio.sourcemanagers.reddit;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.AudioTrackInfoWithImage;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botcommons.web.WebUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.JsonBrowser.NULL_BROWSER;
import static ml.duncte123.skybot.audio.sourcemanagers.reddit.RedditAudioTrack.getPlaybackUrl;
import static ml.duncte123.skybot.utils.AirUtils.isURL;

public class RedditAudioSourceManager extends AbstractDuncteBotHttpSource {
    private static final Pattern FULL_LINK_REGEX = Pattern.compile("https:\\/\\/(?:www|old)\\.reddit\\.com\\/r\\/(?:[^\\/]+)\\/(?:[^\\/]+)\\/([^\\/]+)(?:\\/?(?:[^\\/]+)?\\/?)?");
    private static final Pattern VIDEO_LINK_REGEX = Pattern.compile("https:\\/\\/v\\.redd\\.it\\/([^\\/]+)(?:.*)?");

    public RedditAudioSourceManager() {
        this.configureBuilder(
            (builder) -> builder.setUserAgent(WebUtils.getUserAgent())
        );
    }

    @Override
    public String getSourceName() {
        return "reddit";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        final String identifier = reference.identifier;
        final Matcher fullLink = FULL_LINK_REGEX.matcher(identifier);

        // If it is a full link to a reddit post we can extract the id easily
        // and send that to fetch the json and build the track
        if (fullLink.matches()) {
            final String group = fullLink.group(fullLink.groupCount());
            final JsonBrowser data = this.fetchJson(group);

            return this.buildTrack(data, identifier);
        }

        final Matcher videoLink = VIDEO_LINK_REGEX.matcher(identifier);

        // If we have a short video link we firstly need to follow all redirects
        if (videoLink.matches()) {
            // Once we have the link we can extract the post id and build the track the normal way
            final String actualRedditUrl = this.fetchRedirectUrl(identifier);
            final String id = this.getPostId(actualRedditUrl);
            final JsonBrowser data = this.fetchJson(id);

            return this.buildTrack(data, actualRedditUrl);
        }

        return null;
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
        return new RedditAudioTrack(trackInfo, this);
    }

    private String getPostId(String url) {
        final Matcher matcher = FULL_LINK_REGEX.matcher(url);

        if (matcher.matches()) {
            return matcher.group(matcher.groupCount());
        }

        return url;
    }

    private String fetchRedirectUrl(String vRedditUrl) {
        final HttpGet httpGet = new HttpGet(vRedditUrl);
        final HttpInterface httpInterface = this.getHttpInterface();

        // Follow all redirects until there are no more to follow and return that
        try (final CloseableHttpResponse ignored = httpInterface.execute(httpGet)) {
            return httpInterface.getFinalLocation().toString();
        }
        catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Could not load data from reddit", COMMON, e);
        }
    }

    @Nullable
    private JsonBrowser fetchJson(String pageURl) {
        // Fetch the json from the reddit api so we don't get any useless stuff we don't care about
        final HttpGet httpGet = new HttpGet("https://api.reddit.com/api/info/?id=t3_" + pageURl);

        try (final CloseableHttpResponse response = this.getHttpInterface().execute(httpGet)) {
            final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            final JsonBrowser child = JsonBrowser.parse(content).get("data").get("children").index(0);

            // If we have nothing in the children array we can safely return null
            if (child.equals(NULL_BROWSER)) {
                return null;
            }

            return child.get("data");
        }
        catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Could not load data from reddit", COMMON, e);
        }
    }

    private boolean canPlayAudio(String id) {
        final HttpGet httpGet = new HttpGet(getPlaybackUrl(id));

        // Probe the audio and check the response code, if it is 200 we have some audio
        try (final CloseableHttpResponse response = this.getHttpInterface().execute(httpGet)) {
            return response.getStatusLine().getStatusCode() == 200;
        }
        catch (IOException e) {
            return false;
        }
    }

    private RedditAudioTrack buildTrack(@Nullable JsonBrowser data, String pageURl) {
        if (data == null) {
            return null;
        }

        final String postHint = data.get("post_hint").safeText();

        if (!"hosted:video".equals(postHint)) {
            throw new FriendlyException("This video is not hosted on the reddit website," +
                " only videos hosted on the reddit website can be played", COMMON, null);
        }

        final JsonBrowser media = data.get("media").get("reddit_video");
        final String url = data.get("url").safeText();

        final Matcher videoLink = VIDEO_LINK_REGEX.matcher(url);

        if (!videoLink.matches()) {
            return null;
        }

        final String videoId = videoLink.group(videoLink.groupCount());

        // Probe the audio to check if we can actually play it (there's probably a better way with the dash playlists)
        if (!this.canPlayAudio(videoId)) {
            throw new FriendlyException("This video does not have audio", COMMON, null);
        }

        String thumbnail = data.get("thumbnail").safeText();

        if (!isURL(thumbnail)) {
            thumbnail = "https://www.redditstatic.com/reddit404e.png";
        }

        return new RedditAudioTrack(
            new AudioTrackInfoWithImage(
                data.get("title").safeText(),
                "u/" + data.get("author").safeText(),
                Long.parseLong(media.get("duration").safeText()) * 1000,
                videoId,
                false,
                pageURl,
                thumbnail
            ),
            this
        );
    }
}
