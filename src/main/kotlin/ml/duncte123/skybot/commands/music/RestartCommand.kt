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

import me.duncte123.botCommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

@Author(nickname = "ramidzkh", author = "Ramid Khan")
class RestartCommand : MusicCommand() {

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (!channelChecks(event, ctx.audioUtils))
            return

        val player = getMusicManager(event.guild, ctx.audioUtils).player

        if (player.playingTrack == null) {
            MessageUtils.sendError(event.message)
            MessageUtils.sendMsg(event, "No track currently playing")
            return
        }

        player.seekTo(0)
        MessageUtils.sendSuccess(event.message)
    }

    override fun help() = "Start the current track back to the beginning"

    override fun getName() = "restart"
}