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

package ml.duncte123.skybot.objects.audiomanagers.clypit;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import org.json.JSONObject;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class ClypitAudioSourceManager extends HttpAudioSourceManager {

    private static final Pattern CLYPIT_REGEX = Pattern.compile("(http://|https://(www\\.)?)?clyp\\.it/(.*)");

    @Override
    public String getSourceName() {
        return "clyp.it";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        Matcher m = CLYPIT_REGEX.matcher(reference.identifier);
        if (!m.matches()) {
            return null;
        }

        try {
            String clypitId = m.group(m.groupCount());
            JSONObject json = WebUtils.ins.getJSONObject("https://api.clyp.it/" + clypitId).execute();
            return new AudioReference(json.getString("SecureMp3Url"), json.getString("Title"));
        } catch (Exception e) {
            return null;
        }
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
        return new ClypitAudioTrack(trackInfo, this);
    }
}
