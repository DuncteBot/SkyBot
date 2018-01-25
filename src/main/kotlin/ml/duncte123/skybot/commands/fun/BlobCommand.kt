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

package ml.duncte123.skybot.commands.`fun`

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.WebUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

class BlobCommand : Command() {
    
    init {
        this.category = CommandCategory.FUN
    }
    
    override fun executeCommand(invoke: String, args: Array<String>, event: GuildMessageReceivedEvent) {
        
        var blob = "blobnomcookie"
        
        if (args.isNotEmpty()) {
            blob = StringUtils.join(*args)
        }
        
        WebUtils.getRequest("https://i.duncte123.ml/blob/$blob.png") {
            val responseBody = this!!.body()

            if (responseBody!!.contentLength() <= 0) {
                sendMsg(event, "This blob was not found on the server!!!")
                this.close()
                return@getRequest
            }

            event.channel.sendFile(responseBody.byteStream(), "blob.png", null).queue { this.close() }
        }

        


    }
    
    override fun help() = "Gives you a blob.\n" +
            "Usage: `$PREFIX$name [blob name]`"
    
    override fun getName() = "blob"
}