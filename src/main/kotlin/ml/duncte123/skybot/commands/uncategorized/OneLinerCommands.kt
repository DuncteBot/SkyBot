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
            "invite" -> sendMsg(event, "Invite me with this link:\n<https://lnk.dunctebot.com/invite>")

            "uptime" -> sendMsg(event, AirUtils.getUptime(ManagementFactory.getRuntimeMXBean().uptime, true))

            "quote" -> WebUtils.ins.getText("http://inspirobot.me/api?generate=true").async {
                sendEmbed(event, EmbedUtils.embedImage(it))
            }

            "screenfetch" -> sendMsg(event, "```\n${Runtime.getRuntime().exec("screenfetch -N")
                .getString().replace("`", "​'").replace("\u001B\\[[;\\d]*m", "")}```")

            "website" -> sendMsg(event, "My website is <https://dunctebot.com>")

            else -> println("Invoke was invalid: ${ctx.invoke}")
        }
    }

    override fun help(invoke: String, prefix: String): String {

        return when (invoke) {
            "cookie" -> {
                """blobnomcookie
                    |Usage: `$prefix$invoke`
                """.trimMargin()
            }
            "trigger" -> {
                """Use when you are triggered.
                    |Usage: `$prefix$invoke`
                """.trimMargin()
            }
            "spam" -> {
                """What do you think 😏
                    |Usage: `$prefix$invoke`
                """.trimMargin()
            }
            "wam" -> {
                """you need more WAM!
                    |Usage: `$prefix$invoke`
                """.trimMargin()
            }
            "mineh" -> {
                """HERE COMES MINEH!
                    |Usage: `$prefix$invoke`
                """.trimMargin()
            }
            "invite" -> {
                """Gives you the bot invite
                    |Usage: `$prefix$invoke`
                """.trimMargin()
            }
            "uptime" -> {
                """Shows the bot uptime
                    |Usage: `$prefix$invoke`
                """.trimMargin()
            }
            "quote" -> {
                """Shows an inspiring quote
                    |Usage: `$prefix$invoke`
                """.trimMargin()
            }
            "website" -> {
                """Shows the bots website
                    |Usage: `$prefix$invoke`
                """.trimMargin()
            }

            else -> "invalid invoke"
        }
    }

    override fun help(prefix: String) = """`${prefix}cookie` => blobnomcookie.
            |`${prefix}trigger` => Use when you are triggered.
            |`${prefix}spam` => What do you think 😏
            |`${prefix}wam` => You need more WAM!.
            |`${prefix}mineh` => HERE COMES MINEH!
            |`${prefix}invite` => Gives you the bot invite
            |`${prefix}uptime` => Shows the bot uptime
            |`${prefix}quote` => Shows an inspiring quote
            |`${prefix}website` => Shows the bots website
    """.trimMargin()

    override fun getName() = "cookie"

    override fun getAliases() = arrayOf("trigger", "spam", "wam", "mineh", "invite", "uptime", "quote", "xkcd", "screenfetch", "website")
}
