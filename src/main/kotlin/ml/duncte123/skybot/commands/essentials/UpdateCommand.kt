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
import me.duncte123.botCommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.BotListener
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import ml.duncte123.skybot.utils.MessageUtils.sendMsg
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.*

@Author(author = "Ramid Khan", nickname = "ramidzkh")
class UpdateCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (!isDev(event.author)
                && Settings.OWNER_ID != event.author.id) {
            sendMsg(event, ":x: ***YOU ARE DEFINITELY THE OWNER OF THIS BOT***")
            MessageUtils.sendError(event.message)
            return
        }

        if (!Settings.enableUpdaterCommand) {
            val message = "The updater is not enabled. " +
                    "If you wish to use the updater you need to download it from [this page](https://github.com/ramidzkh/SkyBot-Updater/releases)."
            MessageUtils.sendEmbed(event, EmbedUtils.embedMessage(message))
            return
        }

        /*
         * Tell the bot that we are using the updater
         */
        BotListener.isUpdating = true

        when (args.size) {
            0 -> {
                sendMsg(event, "✅ Updating") {
                    // This will also shutdown eval
                    event.jda.asBot().shardManager.shutdown()

                    // Stop everything that my be using resources
                    AirUtils.stop()

                    // Magic code. Tell the updater to update
                    System.exit(0x54)
                }
            }
            1 -> {
                if (args[0] != "gradle")
                    return
                sendMsg(event, "✅ Updating") {
                    launch {
                        initUpdate(event, it.id)
                    }
                }
            }
        }
    }

    override fun help() = "Update the bot and restart"

    override fun getName() = "update"

    private suspend fun initUpdate(event: GuildMessageReceivedEvent, id: String) {
        lateinit var version: String
        lateinit var links: String

        val updateprogress: Deferred<Boolean> = async(newSingleThreadContext("Update-Coroutine")) {
            val pull = getCommand("git pull")
            val build = getCommand("gradlew build --refresh-dependencies -x test")
            val versioncmd = getCommand("gradlew printVersion")

            links = buildString {
                appendln(runProcess(Runtime.getRuntime().exec(pull)))
                appendln(runProcess(Runtime.getRuntime().exec(build)))
            }


            val process = Runtime.getRuntime().exec(versioncmd)

            val scanner = Scanner(process.inputStream)
            while (scanner.hasNextLine()) {
                val s = scanner.nextLine()
                if (s.matches("[0-9]\\.[0-9]{1,3}\\.[0-9]{1,3}_.{6,9}".toRegex())) {
                    version = s
                    if (process.isAlive) process.destroy()
                    return@async true
                }
            }
            return@async false
        }

        val progress = updateprogress.await()

        if (progress) {
            sendMsg(event, "✅ Update built. Shutting running version down.") {
                event.channel.deleteMessageById(id).queue()
                if (!version.isEmpty()) {
                    // This will also shutdown eval
                    event.jda.asBot().shardManager.shutdown()

                    // Stop everything that my be using resources
                    AirUtils.stop()

                    // Magic code. Tell the updater to update
                    System.exit(0x64)
                }
            }
        } else {
            sendMsg(event, "❌ Update failed building. $links") {
                event.channel.deleteMessageById(id).queue()
            }
        }
    }

    private fun getCommand(cmd: String): String {
        return when {
            System.getProperty("os.name").contains("Windows", false) -> "cmd /C $cmd"
            cmd.startsWith("gradle", false) -> "./$cmd"
            else -> cmd
        }
    }

    private fun runProcess(process: Process): String {
        val scanner = Scanner(process.inputStream)
        val out = buildString {
            while (scanner.hasNextLine()) {
                appendln(scanner.nextLine())
            }
        }
        return WebUtils.ins.hastebin(out).execute()
    }
}
