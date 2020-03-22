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
import me.duncte123.botcommons.messaging.MessageUtils.sendSuccess
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class LeaveCommand : MusicCommand() {

    init {
        this.name = "leave"
        this.aliases = arrayOf("disconnect", "exit", "fuckoff")
        this.help = "Makes the bot leave the current voice channel"
        this.cooldown = musicCooldown
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val guild = event.guild

        if (!getLavalinkManager().isConnected(guild)) {
            sendMsg(event, "I'm not connected to any channels.")
            return
        }

        ctx.audioUtils.removeMusicManager(guild)
        getLavalinkManager().closeConnection(guild)
        guild.audioManager.sendingHandler = null

        sendSuccess(ctx.message)
    }
}
