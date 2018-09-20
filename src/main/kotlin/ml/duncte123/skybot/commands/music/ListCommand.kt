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

package ml.duncte123.skybot.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import java.util.*

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class ListCommand : MusicCommand() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        val mng = getMusicManager(event.guild, ctx.audioUtils)
        val scheduler = mng.scheduler

        val queue: Queue<AudioTrack> = scheduler.queue
        synchronized(queue) {
            if (queue.isEmpty()) {
                sendEmbed(event, EmbedUtils.embedField(ctx.audioUtils.embedTitle, "The queue is currently empty!"))
            } else {
                var queueLength: Long = 0
                val maxTracks = 10
                val sb = StringBuilder()
                sb.append("Current Queue: Entries: ").append(queue.size).append("\n")
                for ((index, track) in queue.withIndex()) {
                    if (index == maxTracks) {
                        break
                    }
                    queueLength += track.duration
                    sb.append("`[").append(AudioUtils.getTimestamp(track.duration)).append("]` ")
                    sb.append(track.info.title).append("\n")
                }
                sb.append("\n").append("Total Queue Time Length: ").append(AudioUtils.getTimestamp(queueLength))
                sendEmbed(event, EmbedUtils.embedField(ctx.audioUtils.embedTitle, sb.toString()))
            }
        }
    }

    override fun help(): String = "shows the current queue"

    override fun getName(): String = "list"

    override fun getAliases(): Array<String> = arrayOf("queue")

}