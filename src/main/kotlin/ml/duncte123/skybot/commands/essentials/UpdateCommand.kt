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

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.*

@Author(author = "Ramid Khan", nickname = "ramidzkh")
class UpdateCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        @Suppress("DEPRECATION")
        if (!Settings.wbkxwkZPaG4ni5lm8laY.contains(event.author.id)
                && Settings.OWNER_ID != event.author.id) {
            MessageUtils.sendMsg(event, ":x: ***YOU ARE DEFINITELY THE OWNER OF THIS BOT***")
            MessageUtils.sendError(event.message)
            return
        }

//        if (!Settings.enableUpdaterCommand) {
//            val message = "The updater is not enabled. " +
//                    "If you wish to use the updater you need to download it from [this page](https://github.com/ramidzkh/SkyBot-Updater/releases)."
//            MessageUtils.sendEmbed(event, EmbedUtils.embedMessage(message))
//            return
//        }

        when (args.size) {
            0 -> {
                MessageUtils.sendMsg(event, "✅ Updating", {
                    // This will also shutdown eval
                    event.jda.asBot().shardManager.shutdown()

                    // Stop everything that my be using resources
                    AirUtils.stop()

                    // Magic code. Tell the updater to update
                    System.exit(0x54)
                })
            }
            1 -> {
                if (args[0] != "gradle")
                    return
                MessageUtils.sendMsg(event, "✅ Updating", {
                    launch {
                        initUpdate(event, it.id)
                    }
                })
            }
        }
    }

    override fun help() = "Update the bot and restart"

    override fun getName() = "update"

    suspend fun initUpdate(event: GuildMessageReceivedEvent, id: String) {
        lateinit var version: String

        val updateprogress: Deferred<Boolean> = async(newSingleThreadContext("Update-Coroutine")) {
            val cmd =
                    if (System.getProperty("os.name").contains("Windows", false))
                        "cmd /C gradlew build --refresh-dependencies"
                    else
                        "./gradlew build --refresh-dependencies"
            val process = Runtime.getRuntime().exec(cmd)
            val scanner = Scanner(process.inputStream)
            while (scanner.hasNextLine()) {
                val s = scanner.nextLine()
                println(s)
                if (s.matches("[0-9]\\.[0-9]{1,3}\\.[0-9]_.{8}".toRegex())) {
                    if (process.isAlive) process.destroy()
                    version = s
                    return@async true
                }
            }
            return@async false
        }

        val progress = updateprogress.await()
        val msg =
                if (progress) "✅ Update built"
                else "❌ Update failed building."
        MessageUtils.sendMsg(event, msg, {
            event.channel.deleteMessageById(id).queue()
        })

        if (progress) {
            MessageUtils.sendMsg(event, "✅ Update built. Shutting running version down.", {
                event.channel.deleteMessageById(id).queue()
                if (!version.isEmpty()) {
                    // This will also shutdown eval
                    event.jda.asBot().shardManager.shutdown()

                    // Stop everything that my be using resources
                    AirUtils.stop()

                    // Magic code. Tell the updater to update
                    System.exit(0x54)
                }
            })
        }
    }
}