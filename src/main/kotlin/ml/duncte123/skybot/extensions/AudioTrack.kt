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

package ml.duncte123.skybot.extensions

import com.dunctebot.sourcemanagers.AudioTrackInfoWithImage
import com.dunctebot.sourcemanagers.getyarn.GetyarnAudioTrack
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioTrack
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.sentry.Sentry
import me.duncte123.botcommons.messaging.EmbedUtils.embedMessage
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.objects.TrackUserData
import ml.duncte123.skybot.utils.MusicEmbedUtils.playerEmbed
import ml.duncte123.skybot.utils.YoutubeUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.sharding.ShardManager

fun AudioTrack.toEmbed(mng: GuildMusicManager, shardManager: ShardManager, withPlayer: Boolean = true): EmbedBuilder {
    val userData = this.getUserData(TrackUserData::class.java)
    var requester = "Unknown"

    if (userData != null) {
        val userId = userData.requester
        val user = shardManager.getUserById(userId)

        if (user != null) {
            requester = user.asTag
        }
    }

    if (this.info.isStream) {
        return embedMessage(
            """**Currently playing** [${this.info.title}](${this.info.uri}) by ${this.info.author}
            |**Requester:** $requester
        """.trimMargin()
        )
            .setThumbnail(this.getImageUrl())
    }

    return embedMessage(
        """**Currently playing** [${this.info.title}](${this.info.uri}) by ${this.info.author}
            |**Requester:** $requester${if (withPlayer) "\n" + playerEmbed(mng) else ""}
        """.trimMargin()
    )
        .setThumbnail(this.getImageUrl())
}

/**
 * @param onlyStatic If we only should return thumbnails that do not require an http request
 */
fun AudioTrack.getImageUrl(onlyStatic: Boolean = false): String? {
    if (this.info is AudioTrackInfoWithImage) {
        return (this.info as AudioTrackInfoWithImage).image
    }

    if (this is YoutubeAudioTrack) {
        return YoutubeUtils.getThumbnail(this.info.identifier)
    }

    if (this is TwitchStreamAudioTrack) {
        return "https://static-cdn.jtvnw.net/previews-ttv/live_user_${this.info.author}-320x180.jpg?r=${System.currentTimeMillis()}"
    }

    /*if (this is BeamAudioTrack) {
        val id = this.identifier.substring(0, this.identifier.indexOf('|'))

        return "https://thumbs.mixer.com/channel/$id.small.jpg?r=${System.currentTimeMillis()}"
    }*/

    if (this is GetyarnAudioTrack) {
        // Gif url https://y.yarn.co/{id}_text.gif
        return "https://y.yarn.co/${this.info.identifier}_screenshot.jpg"
    }

    // The following make a REST request for the thumbnail
    if (!onlyStatic) {
        if (this is VimeoAudioTrack) {
            return try {
                val info = this.info
                val id = info.identifier.substring(info.identifier.lastIndexOf('/') + 1)
                val url = "https://vimeo.com/api/v2/video/$id.json"
                val json = WebUtils.ins.getJSONArray(url).execute()[0]

                json["thumbnail_small"].asText()
            } catch (e: Exception) {
                Sentry.capture(e)
                null
            }
        }

        if (this is SoundCloudAudioTrack ||
            (this is HttpAudioTrack && this.info.uri.startsWith("https://www.pornhub.com/"))
        ) {
            val page = WebUtils.ins.scrapeWebPage(this.info.uri).execute()
            val elems = page.select("meta[property=og:image]")

            if (!elems.isEmpty()) {
                return elems.first().attr("content")
            }
        }
    }

    return null
}
