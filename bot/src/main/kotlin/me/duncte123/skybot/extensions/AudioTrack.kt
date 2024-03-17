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

package me.duncte123.skybot.extensions

import dev.arbjerg.lavalink.client.player.Track
import me.duncte123.botcommons.messaging.EmbedUtils.embedMessage
import me.duncte123.skybot.audio.GuildMusicManager
import me.duncte123.skybot.audio.UUIDUserData
import me.duncte123.skybot.objects.TrackUserData
import me.duncte123.skybot.utils.MusicEmbedUtils.createPlayerString
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.sharding.ShardManager

fun Track.internalClone(): Track {
    val cloned = this.makeClone()

    cloned.setUserData(UUIDUserData())

    return cloned
}

fun Track.toEmbed(
    mng: GuildMusicManager,
    shardManager: ShardManager,
    withPlayer: Boolean = true,
    callback: (EmbedBuilder) -> Unit,
) {
    val userData: TrackUserData? = mng.scheduler.getUserData(this)
    var requester = "Unknown"
    var voteSkipText = ""

    if (userData != null) {
        val userId = userData.requester
        val user = shardManager.getUserById(userId)

        if (user != null) {
            requester = user.asTag
        }

        if (!userData.votes.isEmpty) {
            voteSkipText = "\n\n${userData.votes.size()} people voted to skip"
        }
    }

    val uri = this.info.uri

    if (this.info.isStream) {
        callback(
            embedMessage(
                """**Currently playing** [${this.info.title}]($uri) by ${this.info.author}
                |**Requester:** $requester$voteSkipText
                """.trimMargin()
            )
                .setThumbnail(this.info.artworkUrl)
        )
        return
    }

    createPlayerString(mng) { playerState ->
        callback(
            embedMessage(
                """**Currently playing** [${this.info.title}]($uri) by ${this.info.author}
                |**Requester:** $requester${if (withPlayer) "\n" + playerState else ""}$voteSkipText
                """.trimMargin()
            )
                .setThumbnail(this.info.artworkUrl)
        )
    }
}
