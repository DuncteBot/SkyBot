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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;

public class RedditAudioSourceManager extends AbstractDuncteBotHttpSource {
    private static final String URL_PART = "\\/(?:[^\\/]+)";
    private static final Pattern FULL_LINK_REGEX = Pattern.compile("https:\\/\\/(?:www|old)\\.reddit\\.com\\/r" + URL_PART + URL_PART + "\\/([^\\/]+)(?:\\/?(?:[^\\/]+)?\\/?)?");
    private static final Pattern VIDEO_LINK_REGEX = Pattern.compile("https:\\/\\/v\\.redd\\.it\\/([^\\/]+)(?:.*)?");

    @Override
    public String getSourceName() {
        return "reddit";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        System.out.println(FULL_LINK_REGEX);

        // https://www.reddit.com/r/Corridor/comments/frqtra/noticed_a_little_vfx_mishap_in_the_mandolorian/ // spoiler
        // https://www.reddit.com/r/funny/comments/frn2ar/jack_blacks_quarantine_dance/
        // https://www.reddit.com/r/nextfuckinglevel/comments/frsve4/new_york/
        // https://www.reddit.com/r/cirkeltrek/comments/fro6fp/wat_zei_lubach/
        // https://www.reddit.com/r/nextfuckinglevel/comments/frrtcd/youtuber_luke_towan_creating_an_ultra_realistic/ // no audio
        // https://www.reddit.com/r/me_irl/comments/fs72im/me_irl/ // not a video
        // https://v.redd.it/u30dunqdcsp41

        final String identifier = reference.identifier;
        final Matcher fullLink = FULL_LINK_REGEX.matcher(identifier);

        if (fullLink.matches()) {
            final String group = fullLink.group(fullLink.groupCount());

            System.out.println(group);

            final JsonBrowser data = this.fetchJson(group);

            return this.buildTrack(data, identifier);
        }

        final Matcher videoLink = VIDEO_LINK_REGEX.matcher(identifier);

        if (videoLink.matches()) {
            // we already have id, need to fetch full page for json
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

        try (final CloseableHttpResponse response = httpInterface.execute(httpGet)) {
            return httpInterface.getFinalLocation().toString();
        }
        catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Could not load data from reddit", COMMON, e);
        }
    }

    private JsonBrowser fetchJson(String pageURl) {
        System.out.println("https://api.reddit.com/api/info/?id=t3_" + pageURl);

        final HttpGet httpGet = new HttpGet("https://api.reddit.com/api/info/?id=t3_" + pageURl);

        httpGet.addHeader("User-Agent", WebUtils.getUserAgent());

        try (final CloseableHttpResponse response = this.getHttpInterface().execute(httpGet)) {
            final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

            return JsonBrowser.parse(content).index(0).get("data").get("children").index(0).get("data");
        }
        catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Could not load data from reddit", COMMON, e);
        }
    }

    private RedditAudioTrack buildTrack(JsonBrowser data, String pageURl) {
        final JsonBrowser media = data.get("media").get("reddit_video");
        final String url = data.get("url").safeText();

        final Matcher videoLink = VIDEO_LINK_REGEX.matcher(url);

        if (!videoLink.matches()) {
            return null;
        }

        /*String thumbnail = data.get("thumbnail").safeText();

        if (thumbnail.isBlank() || "default".equals(thumbnail)) {
            thumbnail = "https://www.redditstatic.com/reddit404e.png";
        }*/

        return new RedditAudioTrack(
            new AudioTrackInfoWithImage(
                data.get("title").safeText(),
                "u/" + data.get("author").safeText(),
                Long.parseLong(media.get("duration").safeText()) * 1000,
                videoLink.group(videoLink.groupCount()),
                false,
                pageURl,
                "https://www.redditstatic.com/reddit404e.png" // TODO
            ),
            this
        );
    }
}
