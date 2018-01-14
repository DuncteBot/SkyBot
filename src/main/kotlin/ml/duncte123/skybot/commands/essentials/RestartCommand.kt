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

package ml.duncte123.skybot.commands.essentials

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.slf4j.LoggerFactory

@SinceSkybot("3.50.X")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
class RestartCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
    }
    
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (!Settings.wbkxwkZPaG4ni5lm8laY.contains(event.author.id)) return
        val shardManager = event.jda.asBot().shardManager

        try {
            when (args.size) {
                0 -> shardManager.restart()
                1 -> shardManager.restart(args[0].toInt())
                else -> sendError(event.message)
            }
        } catch (ex: NumberFormatException) {
            if (Settings.useJSON)
                sendErrorJSON(event.message, ex, false)
            else {
                logger.error(ex.localizedMessage, ex)
                sendError(event.message)
            }
        }
    }
    override fun help() = "Restart the bot or a shard\nUsage: $PREFIX$name [shard id]`"

    override fun getName() = "restart"
}
