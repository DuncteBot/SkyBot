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

package ml.duncte123.skybot.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.duncte123.botcommons.messaging.EmbedUtils.embedMessage
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.extensions.getImageUrl
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.MusicEmbedUtils.playerEmbed
import net.dv8tion.jda.core.EmbedBuilder
import java.util.function.BiFunction

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class NowPlayingCommand : MusicCommand() {

    init {
        this.name = "nowplaying"
        this.aliases = arrayOf("np", "song")
        this.helpFunction = BiFunction { _, _ -> "Prints information about the currently playing song (title, current time)" }
    }

    override fun execute(ctx: CommandContext) {
        val event = ctx.event
        val mng = getMusicManager(event.guild, ctx.audioUtils)
        val player = mng.player

        val msg = when {
            player.playingTrack != null -> player.playingTrack.toEmbed(mng)

            else -> embedMessage("The player is not currently playing anything!")
        }

        sendEmbed(event, msg)
    }

    private fun AudioTrack.toEmbed(mng: GuildMusicManager): EmbedBuilder {
        if (this.info.isStream) {
            return embedMessage("**Playing [${this.info.title}](${this.info.uri})**")
                .setThumbnail(this.getImageUrl())
        }

        return  embedMessage("**Playing** [${this.info.title}](${this.info.uri})\n" + playerEmbed(mng))
            .setThumbnail(this.getImageUrl())
    }
}
