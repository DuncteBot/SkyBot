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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Authors
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.extensions.getString
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.core.MessageBuilder
import java.lang.management.ManagementFactory

@Authors(authors = [
    Author(nickname = "Sanduhr32", author = "Maurice R S"),
    Author(nickname = "duncte123", author = "Duncan Sterken"),
    Author(nickname = "ramidzkh", author = "Ramid Khan")
])
class OneLinerCommands : Command() {

    init {
        this.displayAliasesInHelp = true
    }

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        when (ctx.invoke) {
            "cookie" -> sendMsg(event, "<:blobnomcookie_secret:317636549342789632>")

            "trigger" -> sendEmbed(event, EmbedUtils.embedImage("https://cdn.discordapp.com/attachments/94831883505905664/176181155467493377/triggered.gif"))

            "spam" -> sendEmbed(event, EmbedUtils.embedImage("https://cdn.discordapp.com/attachments/191245668617158656/216896372727742464/spam.jpg"))

//            "wam" -> sendEmbed(event, embedField("GET YOUR WAM NOW!!!!", "[http://downloadmorewam.com/](http://downloadmorewam.com/)"))
            "wam" -> sendMsg(event, "http://downloadmorewam.com/wam.mp4")

            "mineh" -> sendMsg(event, MessageBuilder().setTTS(true).append("Insert creepy music here").build()) {
                sendEmbed(event,
                    EmbedUtils.embedImage("https://cdn.discordapp.com/attachments/204540634478936064/213983832087592960/20160813133415_1.jpg")
                )
            }

            // "event.jda.selfUser.id" might be invalid "jda.asBot().getApplicationInfo().complete().id"
            "invite" -> sendMsg(event, "Invite me with this link:\n<https://discordapp.com/oauth2/authorize?client_id=210363111729790977&scope=bot&permissions=-1>")

            "uptime" -> sendMsg(event, AirUtils.getUptime(ManagementFactory.getRuntimeMXBean().uptime, true))

            "quote" -> WebUtils.ins.getText("http://inspirobot.me/api?generate=true").async {
                sendEmbed(event, EmbedUtils.embedImage(it))
            }

            "screenfetch" -> sendMsg(event, "```\n${Runtime.getRuntime().exec("screenfetch -N")
                .getString().replace("`", "​'").replace("\u001B\\[[;\\d]*m", "")}```")

            "xkcd" -> {
                WebUtils.ins.scrapeWebPage("https://c.xkcd.com/random/comic/").async {
                    sendMsg(event, "https:" + it.select("#comic img").attr("src"))
                }
            }

            else -> println("Invoke was invalid: ${ctx.invoke}")
        }
    }

    override fun help(invoke: String?): String {

        return when (invoke) {
            "cookie" -> {
                """blobnomcookie
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "trigger" -> {
                """Use when you are triggered.
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "spam" -> {
                """What do you think 😏
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "wam" -> {
                """you need more WAM!
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "mineh" -> {
                """HERE COMES MINEH!
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "invite" -> {
                """Gives you the bot invite
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "uptime" -> {
                """Shows the bot uptime
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "quote" -> {
                """Shows an inspiring quote
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "xkcd" -> """Get a random comic from xkcd.com
                |Usage: `${Settings.PREFIX}$invoke`
            """.trimMargin()

            else -> "invalid invoke"
        }
    }

    override fun help() = """`${Settings.PREFIX}cookie` => blobnomcookie.
            |`${Settings.PREFIX}trigger` => Use when you are triggered.
            |`${Settings.PREFIX}spam` => What do you think 😏
            |`${Settings.PREFIX}wam` => You need more WAM!.
            |`${Settings.PREFIX}mineh` => HERE COMES MINEH!
            |`${Settings.PREFIX}invite` => Gives you the bot invite
            |`${Settings.PREFIX}uptime` => Shows the bot uptime
            |`${Settings.PREFIX}quote` => Shows an inspiring quote
            |`${Settings.PREFIX}xkcd` => Get a random comic from xkcd.com
    """.trimMargin()

    override fun getName() = "cookie"

    override fun getAliases() = arrayOf("trigger", "spam", "wam", "mineh", "invite", "uptime", "quote", "xkcd", "screenfetch")
}
