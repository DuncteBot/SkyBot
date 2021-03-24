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
        this.help = "Makes the player repeat the currently playing song"
        this.usage = "[playlist/status]"
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val mng = ctx.audioUtils.getMusicManager(event.guild)
        val scheduler = mng.scheduler

        if (ctx.args.size == 1) {
            when (ctx.args[0]) {
                "playlist" -> {
                    scheduler.isRepeatingPlaylists = !scheduler.isRepeatingPlaylists
                    scheduler.isRepeating = scheduler.isRepeatingPlaylists
                }

                "status" -> {
                    sendMsg(
                        ctx,
                        """Current repeat status:
                        |Repeating: **${scheduler.isRepeating.toEmoji()}**
                        |Repeating queue: **${scheduler.isRepeatingPlaylists.toEmoji()}**
                    """.trimMargin()
                    )

                    return
                }

                else -> {
                    this.sendUsageInstructions(ctx)

                    return
                }
            }
        } else {
            // turn off all repeats if they are on
            if (scheduler.isRepeatingPlaylists) scheduler.isRepeatingPlaylists = false
            scheduler.isRepeating = !scheduler.isRepeating
        }

        sendMsg(
            ctx,
            "Player is now set to: **${if (scheduler.isRepeating) "" else "not "}repeating" +
                "${if (scheduler.isRepeatingPlaylists) " this playlist" else ""}**"
        )
    }
}
