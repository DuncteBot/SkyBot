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

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class PauseCommand : MusicCommand() {

    init {
        this.name = "pause"
        this.aliases = arrayOf("resume")
        this.helpFunction = { _, _ -> "Pauses the current song" }
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val mng = getMusicManager(event.guild, ctx.audioUtils)
        val player = mng.player

        if (player.playingTrack == null) {
            sendMsg(event, "Cannot pause or resume player because no track is loaded for playing.")
            return
        }

        player.isPaused = !player.isPaused
        sendMsg(event, "The player has ${if (player.isPaused) "been paused" else "resumed playing"}.")
    }
}
