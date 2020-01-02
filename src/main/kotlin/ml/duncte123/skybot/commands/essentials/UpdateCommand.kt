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

package ml.duncte123.skybot.commands.essentials

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.listeners.ReadyShutdownListener
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.CommandUtils.isDev
import java.lang.System.getProperty
import java.lang.Thread.sleep
import kotlin.system.exitProcess

@Author(author = "Ramid Khan", nickname = "ramidzkh")
class UpdateCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
        this.name = "update"
        this.helpFunction = { _, _ -> "Update the bot and restart" }
    }

    override fun execute(ctx: CommandContext) {
        if (!isDev(ctx.author)
            && Settings.OWNER_ID != ctx.author.idLong) {
            sendMsg(ctx, ":x: ***YOU ARE DEFINITELY THE OWNER OF THIS BOT***")
            MessageUtils.sendError(ctx.message)
            return
        }

        if (getProperty("updater") == null) {
            val message = "The updater is not enabled. " +
                "If you wish to use the updater you need to download it from [this page](https://github.com/ramidzkh/SkyBot-Updater/releases)."
            sendEmbed(ctx, EmbedUtils.embedMessage(message))
            return
        }

        sendMsg(ctx, "✅ Updating") {
            // This will also shutdown eval
            val listener = ctx.jda.eventManager.registeredListeners.find { it.javaClass == ReadyShutdownListener::class.java } as ReadyShutdownListener

            listener.killAllShards(ctx.shardManager!!, false)

            // Wait for 2 seconds to allow everything to shut down
            sleep(2000)

            // Magic code. Tell the updater to update
            exitProcess(0x54)
        }
    }
}
