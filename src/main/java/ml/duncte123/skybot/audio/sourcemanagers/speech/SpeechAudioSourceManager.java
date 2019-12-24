/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.audio.sourcemanagers.speech;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.audio.sourcemanagers.IdentifiedAudioReference;

import java.io.DataInput;
import java.io.DataOutput;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Author(nickname = "ramidzkh", author = "Ramid Khan")
public class SpeechAudioSourceManager extends HttpAudioSourceManager {

    private static final String PREFIX = "speak:";
    private static final String GOOGLE_TRANSLATE_URL = "https://translate.google.com/translate_tts" +
        "?tl=%language%" +
        "&q=%query%" +
        "&ie=UTF-8&total=1&idx=0" +
        "&text" + "len=%length%" +
        "&client=tw-ob";

    private final String templateURL;

    /**
     * @param language
     *         The language and accent code to play back audio in
     */
    public SpeechAudioSourceManager(String language) {
        this.templateURL = GOOGLE_TRANSLATE_URL.replace("%language%", language);
    }

    @Override
    public String getSourceName() {
        return "speak";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        // We check if it's larger so we don't send requests of nothing
        if (!reference.identifier.startsWith(PREFIX) || reference.identifier.length() <= PREFIX.length()) {
            return null;
        }

        String data = reference.identifier.substring(PREFIX.length());
        data = data
            // Remove whitespaces at the end
            .trim()
            // Remove whitespaces at the front
            .replaceAll("^\\s+", "");

        final String encoded = URLEncoder.encode(data, StandardCharsets.UTF_8);

        final String mp3URL = templateURL
            .replace("%length%", Integer.toString(data.length()))
            .replace("%query%", encoded);

        // Redirect to somewhere else
        return new IdentifiedAudioReference(mp3URL, reference.identifier, "Speaking " + data);
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
}
