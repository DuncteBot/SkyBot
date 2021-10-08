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

package ml.duncte123.skybot.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AudioUtils.getTimestamp
import org.apache.commons.lang3.StringUtils
import java.util.*
import kotlin.math.min

class QueueCommand : MusicCommand() {

    init {
        this.name = "list"
        this.aliases = arrayOf("queue", "q")
        this.help = "Shows the current queue"
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val mng = ctx.audioUtils.getMusicManager(event.guild)
        val scheduler = mng.scheduler
        val queue: Queue<AudioTrack> = scheduler.queue
        val playingTrack = mng.player.playingTrack

        synchronized(queue) {
            val playing = if (playingTrack == null) "Nothing" else "${playingTrack.info.title} by ${playingTrack.info.author}"
            val current = "**Currently playing:** $playing"

            if (queue.isEmpty()) {
                sendEmbed(ctx, EmbedUtils.embedMessage("$current\n**Queue:** Empty"))
            } else {
                val queueLength = queue.sumOf { it.duration }
                val maxTracks = 10
                val queueText = buildString {
                    appendLine(current)
                    appendLine("**Queue:** Showing **${min(maxTracks, queue.size)}**/**${queue.size}** tracks")

                    for ((index, track) in queue.withIndex()) {
                        if (index == maxTracks) {
                            break
                        }

                        appendLine(StringUtils.abbreviate("`[${getTimestamp(track.duration)}]` ${track.info.title}", 60))
                    }

                    appendLine("Total Queue Time Length: ${getTimestamp(queueLength)}")
                    appendLine("Hint: Use `${ctx.prefix}save` to save the current queue to a file that you can re-import")
                }

                sendEmbed(ctx, EmbedUtils.embedMessage(queueText))
            }
        }
    }
}
