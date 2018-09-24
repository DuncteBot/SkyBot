/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.objects.audiomanagers.speech;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ml.duncte123.skybot.Author;

import java.io.DataInput;
import java.io.DataOutput;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Author(nickname = "ramidzkh", author = "Ramid Khan")
public class SpeechAudioSourceManager implements AudioSourceManager {

    private static final String PREFIX = "speak:";
    private static final String GOOGLE_TRANSLATE_URL = "https://translate.google.com/translate_tts" +
            "?tl=%language%" +
            "&q=%query%" +
            "&ie=UTF-8&total=1&idx=0" +
            "&text" + "len=%length%" +
            "&client=tw-ob";

    private final int limit;
    private final String templateURL;
    private final HttpInterfaceManager httpInterfaceManager;

    /**
     * @param limit The character limit of the text, not including prepended or trailing whitespaces
     * @param language The language and accent code to play back audio in
     */
    public SpeechAudioSourceManager(int limit, String language) {
        this.limit = limit;
        this.templateURL = GOOGLE_TRANSLATE_URL.replace("%language%", language);
        httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
    }

    @Override
    public String getSourceName() {
        return "speak";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        // We check if it's larger so we don't send requests of nothing
        if(reference.identifier.startsWith(PREFIX)
                && reference.identifier.length() > PREFIX.length()) {
            String data = reference.identifier.substring(PREFIX.length());
            data = data
                    // Remove whitespaces at the end
                    .trim()
                    // Remove whitespaces at the front
                    .replaceAll("^\\s+", "")
                    // Limit the length
                    /*.substring(0, Math.min(data.length() - 1, limit))*/;

            String encoded = URLEncoder.encode(data, StandardCharsets.UTF_8);

            String mp3URL = templateURL
                    .replace("%length%", Integer.toString(data.length()))
                    .replace("%query%", encoded);

            // Redirect to somewhere else
            return new AudioReference(mp3URL, "Speaking " + data);
        }

        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // empty because we don't need them
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new SpeechAudioTrack(trackInfo, this);
    }

    @Override
    public void shutdown() {
        // empty because we don't need them
    }

    HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }
}
