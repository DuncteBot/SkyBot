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

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.extensions.toEmoji
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

class RepeatCommand : MusicCommand() {

    init {
        this.name = "repeat"
        this.aliases = arrayOf("loop")
        this.help = "Makes the player repeat the currently playing song or the entire queue"
        this.usage = "[playlist/queue/status]"
    }

    override fun run(ctx: CommandContext) {
        val mng = ctx.audioUtils.getMusicManager(ctx.guild)
        val scheduler = mng.scheduler

        if (ctx.args.size == 1) {
            when (ctx.args[0]) {
                "playlist", "queue" -> {
                    val wasLooping = scheduler.isLooping

                    scheduler.isLoopingQueue = !scheduler.isLoopingQueue
                    scheduler.isLooping = false

                    var extra = ""

                    if (wasLooping) {
                        extra = "\n(Normal looping has been turned off)"
                    }

                    sendMsg(
                        ctx,
                        "Player is now set to: **${if (scheduler.isLoopingQueue) "" else "not "}repeating the current queue**$extra"
                    )
                }

                "status" -> {
                    sendMsg(
                        ctx,
                        """Current repeat status:
                        |Repeating: **${scheduler.isLooping.toEmoji()}**
                        |Repeating queue: **${scheduler.isLoopingQueue.toEmoji()}**
                        """.trimMargin()
                    )
                }

                else -> {
                    this.sendUsageInstructions(ctx)
                }
            }

            return
        }

        val wasLoopingQueue = scheduler.isLoopingQueue

        // turn off all looping
        if (scheduler.isLoopingQueue || scheduler.isLooping) {
            scheduler.isLoopingQueue = false
            scheduler.isLooping = false
        }

        scheduler.isLooping = !scheduler.isLooping

        sendMsg(
            ctx,
            "Player is now set to: **${if (scheduler.isLooping) "" else "not "}repeating" +
                "${if (wasLoopingQueue) " the current queue" else ""}**"
        )
    }
}
