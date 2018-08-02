/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.MessageUtils


class VolumeCommand : MusicCommand() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event
        val args = ctx.args

        if (!isUserOrGuildPatron(event))
            return

        if (!channelChecks(event))
            return

        val mng = getMusicManager(event.guild)
        val player = mng.player

        if (args.isEmpty()) {
            MessageUtils.sendMsg(event, "The current volume is **${player.volume}**")
        } else {
            try {
                val newVolume = Math.max(10, Math.min(100, Integer.parseInt(args[0])))
                val oldVolume = player.volume
                player.volume = newVolume
                MessageUtils.sendMsg(event, "Player volume changed from **$oldVolume** to **$newVolume**")
            } catch (e: NumberFormatException) {
                MessageUtils.sendMsg(event, "**${args[0]}** is not a valid integer. (10 - 100)")
            }
        }
    }

    override fun help() = """Sets the new volume on the player.
        |Usage: `$PREFIX$name [new volume]`
    """.trimMargin()

    override fun getName() = "volume"
}