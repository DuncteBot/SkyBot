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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.commands.music

import me.duncte123.botCommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class LeaveCommand : MusicCommand() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (!channelChecks(event, ctx.audioUtils))
            return
        val guild = event.guild

        if (hasCoolDown(guild) && !isPatron(ctx.author, null)) {
            MessageUtils.sendMsg(event, """I still have cooldown!
                    |Remaining cooldown: ${cooldowns[guild.idLong].toDouble() / 1000}s""".trimMargin())
            MessageUtils.sendError(event.message)
            return
        }
        val manager = getMusicManager(guild, ctx.audioUtils)

        if (getLavalinkManager().isConnected(guild)) {
            manager.player.stopTrack()
            getLavalinkManager().closeConnection(guild)
            guild.audioManager.sendingHandler = null
            MusicCommand.addCooldown(guild.idLong)
            if (guild.audioManager.connectionListener != null)
                guild.audioManager.connectionListener = null
            MessageUtils.sendMsg(event, "Leaving your channel")
        } else {
            MessageUtils.sendMsg(event, "I'm not connected to any channels.")
        }
    }

    override fun help(): String = "Makes the bot leave your channel."

    override fun getName(): String = "leave"

    override fun getAliases(): Array<String> = arrayOf("disconnect", "exit")
}