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

package ml.duncte123.skybot.extensions

import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioTrack
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioTrack
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.objects.audiomanagers.AudioTrackInfoWithImage
import ml.duncte123.skybot.objects.audiomanagers.spotify.SpotifyAudioTrack

/**
 * @param onlyStatic If we only should return thumbnails that do not require an http request
 */
fun AudioTrack.getImageUrl(onlyStatic: Boolean = false): String? {

    if (this is SpotifyAudioTrack && this.info is AudioTrackInfoWithImage) {
        return (this.info as AudioTrackInfoWithImage).image
    }

    if (this is YoutubeAudioTrack) {
        return "https://i.ytimg.com/vi/${this.identifier}/mqdefault.jpg"
    }

    if (this is TwitchStreamAudioTrack) {
        return "https://static-cdn.jtvnw.net/previews-ttv/live_user_${this.info.author}-320x180.jpg?r=${System.currentTimeMillis()}"
    }

    if (this is BeamAudioTrack) {
        val id = this.identifier.split("|")[0]

        return "https://thumbs.mixer.com/channel/$id.small.jpg?r=${System.currentTimeMillis()}"
    }

    // The following make a REST request for the thumbnail

    if (this is VimeoAudioTrack && !onlyStatic) {
        return try {
            val split = this.identifier.split("/")
            val id = split[split.size - 1]
            val url = "https://vimeo.com/api/v2/video/$id.json"
            val json = WebUtils.ins.getJSONArray(url).execute().get(0)

            json.get("thumbnail_small").asText()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    if (this is SoundCloudAudioTrack && !onlyStatic) {
        val page = WebUtils.ins.scrapeWebPage(this.info.uri).execute()
        val elems = page.select("meta[property=og:image]")

        if (!elems.isEmpty()) {
            return elems.first().attr("content")
        }
    }

    return null
}
