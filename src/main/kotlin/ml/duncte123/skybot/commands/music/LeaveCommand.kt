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

import me.duncte123.botcommons.messaging.MessageUtils.sendError
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class LeaveCommand : MusicCommand() {

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val guild = event.guild

        if (hasCoolDown(guild) && !isUserOrGuildPatron(event, false)) {
            sendMsg(event, """I still have cooldown!
                    |Remaining cooldown: ${cooldowns[guild.idLong].toDouble() / 1000}s""".trimMargin())
            sendError(event.message)
            return
        }

        val manager = getMusicManager(guild, ctx.audioUtils)

        if (!getLavalinkManager().isConnected(guild)) {
            sendMsg(event, "I'm not connected to any channels.")
            return
        }

        manager.player.stopTrack()
        getLavalinkManager().closeConnection(guild)
        guild.audioManager.sendingHandler = null
        addCooldown(guild.idLong)

        sendMsg(event, "Leaving your channel")

    }

    override fun help(prefix: String): String? = "Makes the bot leave your channel."

    override fun getName(): String = "leave"

    override fun getAliases(): Array<String> = arrayOf("disconnect", "exit")
}
