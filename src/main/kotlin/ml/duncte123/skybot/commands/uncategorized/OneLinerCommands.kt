/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.lang.management.ManagementFactory
import java.time.temporal.ChronoUnit

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
        val args = ctx.args

        when (ctx.invoke) {
            "ping" -> pingCommand(event)

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
            "invite" -> sendMsg(event, "Invite me with this link:\n<https://discordapp.com/oauth2/authorize?client_id=${event.jda.selfUser.id}&scope=bot&permissions=8>")

            "uptime" -> sendMsg(event, AirUtils.getUptime(ManagementFactory.getRuntimeMXBean().uptime, true))

            "quote" -> WebUtils.ins.getText("http://inspirobot.me/api?generate=true").async {
                sendEmbed(event, EmbedUtils.embedImage(it))
            }
            "yesno" -> yesnoCommand(event)


            "donate" -> donateCommand(args, event)

            "screenfetch" -> sendMsg(event, "```\n${screenFetchCommand()}```")
            //val test =  "```${"screenfetch -N".execute().text.replaceAll("`", "​'").replaceAll("\u001B\\[[;\\d]*m", "")}```"

            "insta" -> instaCommand(args, event)

            "xkcd" -> {
                WebUtils.ins.scrapeWebPage("https://c.xkcd.com/random/comic/").async {
                    sendMsg(event, "https:" + it.select("#comic img").attr("src"))
                }
            }

            "reverse" -> {
                if (args.isEmpty()) {
                    sendMsg(event, "Missing arguments")
                    return
                }
                sendMsg(event, ctx.argsRaw.reversed())
            }

            else -> println("Invoke was invalid: ${ctx.invoke}")
        }
    }

    private fun donateCommand(args: List<String>, event: GuildMessageReceivedEvent) {
        val amount = if (args.isNotEmpty()) "/" + args.joinToString(separator = "") else ""
        sendMsg(event, """Hey there thank you for your interest in supporting the bot.
                        |You can use one of the following methods to donate:
                        |**PayPal:** <https://paypal.me/duncte123$amount>
                        |**Patreon:** <https://patreon.com/DuncteBot>
                        |
                        |All donations are going directly into development of the bot ❤
                    """.trimMargin())
    }

    private fun instaCommand(args: List<String>, event: GuildMessageReceivedEvent) {
        //LoggerFactory.getLogger(OneLinerCommands::class.java).error("THIS IS NO ONELINER!") // neither are some of the other commands in here
        val username = if (args.isNotEmpty()) args.joinToString(separator = "") else "duncte123"
        WebUtils.ins.getJSONObject("https://apis.duncte123.me/insta/$username").async {
            if (it.getJSONArray("images").length() < 1) {
                sendMsg(event, "No data found for this user")
            } else {
                val img = it.getJSONArray("images").getJSONObject(0)
                sendEmbed(event, EmbedUtils.defaultEmbed()
                    .setAuthor(it.getJSONObject("user").getString("username"), null
                        , it.getJSONObject("user").getString("profile_pic_url"))
                    .setTitle("Latest picture of $username", "https://instagram.com/$username/")
                    .setDescription(img.getString("caption"))
                    .setImage(img.getString("url")))
            }
        }
    }

    private fun yesnoCommand(event: GuildMessageReceivedEvent) {
        WebUtils.ins.getJSONObject("https://yesno.wtf/api").async {
            sendEmbed(event, EmbedUtils.defaultEmbed()
                .setTitle(it.getString("answer"))
                .setImage(it.getString("image"))
                .build())
        }
    }

    private fun pingCommand(event: GuildMessageReceivedEvent) {
        sendMsg(event, "PONG!") {
            it.editMessage("PONG!\n" +
                "Rest ping: ${event.message.creationTime.until(it.creationTime, ChronoUnit.MILLIS)}ms\n" +
                "Websocket ping: ${event.jda.ping}ms\n" +
                "Average shard ping: ${event.jda.asBot().shardManager.averagePing}ms").queue()
        }
    }

    private fun screenFetchCommand(): String {
        val command = Runtime.getRuntime().exec("screenfetch -N").getString()
//        val command = Runtime.getRuntime().exec("help").getString()
        return command.replace("`", "​'").replace("\u001B\\[[;\\d]*m", "")
    }

    override fun help(invoke: String?): String {

        return when (invoke) {
            "ping" -> {
                """Pong
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
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
            "yesno" -> {
                """Chooses between yes or no
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "donate" -> {
                """Gives you a link to donate for the bot
                    |Usage: `${Settings.PREFIX}$invoke [amount]`
                """.trimMargin()
            }
            "insta" -> {
                """Get the latest picture of someones profile
                    |Usage: `${Settings.PREFIX}$invoke [username]`
                """.trimMargin()
            }
            "xkcd" -> """Get a random comic from xkcd.com
                |Usage: `${Settings.PREFIX}$invoke`
            """.trimMargin()
            "reverse" -> """Reverses a string
                |Usage: `${Settings.PREFIX}$invoke <text>`
            """.trimMargin()
            else -> "invalid invoke"
        }
    }

    override fun help() = """`${Settings.PREFIX}ping` => Shows the delay from the bot to the discord servers.
            |`${Settings.PREFIX}cookie` => blobnomcookie.
            |`${Settings.PREFIX}trigger` => Use when you are triggered.
            |`${Settings.PREFIX}spam` => What do you think 😏
            |`${Settings.PREFIX}wam` => You need more WAM!.
            |`${Settings.PREFIX}mineh` => HERE COMES MINEH!
            |`${Settings.PREFIX}invite` => Gives you the bot invite
            |`${Settings.PREFIX}uptime` => Shows the bot uptime
            |`${Settings.PREFIX}quote` => Shows an inspiring quote
            |`${Settings.PREFIX}yesno` => Chooses between yes or no
            |`${Settings.PREFIX}donate [amount]` => Gives you a link to donate for the bot
            |`${Settings.PREFIX}insta [amount]` => Get the latest picture of someones profile
            |`${Settings.PREFIX}xkcd` => Get a random comic from xkcd.com
            |`${Settings.PREFIX}reverse <text>` => reverses a string
    """.trimMargin()

    override fun getName() = "ping"

    override fun getAliases() = arrayOf("cookie", "trigger", "spam", "wam", "mineh", "invite", "uptime", "quote", "yesno",
        "insta", "donate", "insta", "xkcd", "reverse", "screenfetch")
}
