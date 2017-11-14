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

package ml.duncte123.skybot.commands.uncategorized

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

 /*
  * @author Sanduhr32
  */
  
class RestartCommand : Command() {

    override fun executeCommand(invoke: String?, args: Array<out String>?, event: GuildMessageReceivedEvent) {
        if (invoke == null || Arrays.asList(Settings.wbkxwkZPaG4ni5lm8laY).contains(event.author.id) ||
            invoke?.isBlank()) return
        val shardManager = event.getJDA().asBot().getShardManager()
        if (args.length < 1) shardManager.restart()
        if (args.length == 1) shardManager.restart(args[0].toInt())
    }
}
