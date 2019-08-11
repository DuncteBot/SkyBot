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

package ml.duncte123.skybot.commands.essentials

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.listeners.BaseListener
import ml.duncte123.skybot.listeners.MessageListener
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.CommandUtils.isDev
import java.lang.System.getProperty
import java.lang.Thread.sleep
import java.util.function.BiFunction
import kotlin.system.exitProcess

@Author(author = "Ramid Khan", nickname = "ramidzkh")
class UpdateCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
        this.name = "update"
        this.helpFunction = BiFunction { _, _ -> "Update the bot and restart" }
    }

    override fun execute(ctx: CommandContext) {
        val event = ctx.event

        if (!isDev(event.author)
            && Settings.OWNER_ID != event.author.idLong) {
            sendMsg(event, ":x: ***YOU ARE DEFINITELY THE OWNER OF THIS BOT***")
            MessageUtils.sendError(event.message)
            return
        }

        if (getProperty("updater") == null) {
            val message = "The updater is not enabled. " +
                "If you wish to use the updater you need to download it from [this page](https://github.com/ramidzkh/SkyBot-Updater/releases)."
            sendEmbed(event, EmbedUtils.embedMessage(message))
            return
        }

        /*
         * Tell the bot that we are using the updater
         */
        BaseListener.isUpdating = true
        BaseListener.shuttingDown = true

        sendMsg(event, "✅ Updating") {
            // This will also shutdown eval
            val listener = event.jda.eventManager.registeredListeners.find { it.javaClass == MessageListener::class.java } as MessageListener

            listener.killAllShards(event.jda.shardManager!!)

            // Wait for 2 seconds to allow everything to shut down
            sleep(2000)

            // Magic code. Tell the updater to update
            exitProcess(0x54)
        }
    }
}
