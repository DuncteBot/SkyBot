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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class PauseCommand : MusicCommand() {
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        if (!channelChecks(event))
            return

        val player = getMusicManager(event.guild).player

        if (player.playingTrack == null) {
            sendMsg(event, "Cannot pause or resume player because no track is loaded for playing.")
            return
        }

        player.isPaused = !player.isPaused
        sendMsg(event, "The player has ${if (player.isPaused) "been paused" else "resumed playing"}.")
    }

    override fun help(): String = "Pauses the current song"

    override fun getName(): String = "pause"
}