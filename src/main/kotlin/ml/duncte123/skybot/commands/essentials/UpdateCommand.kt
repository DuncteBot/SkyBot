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

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.Settings
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class UpdateCommand: Command() {

    init {
        this.category = CommandCategory.UNLISTED
    }
    
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        @Suppress("DEPRECATION")
        if (!Settings.wbkxwkZPaG4ni5lm8laY.contains(event.author.id)
                && Settings.ownerId != event.author.id) {
            event.channel.sendMessage(":x: ***YOU ARE DEFINITELY THE OWNER OF THIS BOT***").queue()
            sendError(event.message)
            return
        }

        if(!Settings.enableUpdaterCommand) {
            val message = "The updater is not enabled. " +
                    "If you wish to use the updater you need to download it from [this page](https://github.com/ramidzkh/SkyBot-Updater/releases)."
            sendEmbed(event, EmbedUtils.embedMessage(message))
            return
        }
        
        sendMsg(event, "✅ Updating")
        
        // This will also shutdown eval
        event.jda.asBot().shardManager.shutdown()
        
        // Stop everything that my be using resources
        AirUtils.stop()
        
        // Magic code. Tell the updater to update
        System.exit(0x54)
    }

    override fun help()= "Update the bot and restart"
    
    override fun getName()= "update"
}