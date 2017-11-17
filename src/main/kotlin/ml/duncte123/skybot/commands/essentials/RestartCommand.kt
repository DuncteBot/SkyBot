/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

import ml.duncte123.skybot.BotListener
import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.*

/*
 * @author Sanduhr32
 */
  
class RestartCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
    }

    override fun executeCommand(invoke: String?, args: Array<out String>?, event: GuildMessageReceivedEvent) {
        if (!Arrays.asList(Settings.wbkxwkZPaG4ni5lm8laY).contains(event.author.id)) return
        val shardManager = event.jda.asBot().shardManager

//        val list = event.jda.registeredListeners.filter { t -> t is BotListener}.map {t -> t as BotListener}
//
//        if(list.isEmpty()) {
//            sendMsg(event, "There are no command listeners in here?")
//            sendError(event.message)
//            return
//        }

//        val restart = list[0].restart

//        if(args!!.isEmpty()) {
            // Stop all shards
//            shardManager.shutdown()
//            if (!restart) {
                // Clean the variables
//                AirUtils.reload()
                // Re-run the main command
//                SkyBot.main()
//            } else
                // Magic code, send the restart signal to the executing program
//                System.exit(0x5454)
//        } else try {
//            shardManager.shutdown(args[0].toInt())
//        } catch (e: NumberFormatException) {
//            sendMsg(event, "Invalid shard number")
//            sendError(event.message)
//        }
        if (args == null) {
            error("args is null?!")
            return
        }
        
        when (args.size) {
            0 -> shardManager.restart()
            1 -> shardManager.restart(args[0].toInt())
            else -> sendError("Args is to big! Lenght: ${args.size}")
        }
    }
    override fun help() = "Restart the bot or a shard\nUsage: $PREFIX$name [shard id]`"

    override fun getName() = "restart"
}
