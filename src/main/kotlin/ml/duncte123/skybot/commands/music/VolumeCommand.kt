/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron
import kotlin.math.max
import kotlin.math.min

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class VolumeCommand : MusicCommand() {

    init {
        this.name = "volume"
        this.helpFunction = { _, _ -> "Sets the volume on the music player" }
        this.usageInstructions = { prefix, invoke -> "`$prefix$invoke [volume]`" }
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val args = ctx.args

        if (!isUserOrGuildPatron(event)) {
            return
        }

        val mng = getMusicManager(event.guild, ctx.audioUtils)
        val player = mng.player

        if (args.isEmpty()) {
            sendMsg(event, "The current volume is **${player.volume}**")
            return
        }

        try {
            val newVolume = max(5, min(1000, Integer.parseInt(args[0])))
            val oldVolume = player.volume

            player.volume = newVolume

            sendMsg(event, "Player volume changed from **$oldVolume** to **$newVolume**")
        } catch (e: NumberFormatException) {
            sendMsg(event, "**${args[0]}** is not a valid integer. (5 - 1000)")
        }

    }
}
