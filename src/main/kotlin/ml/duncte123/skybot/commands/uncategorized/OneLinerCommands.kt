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

package ml.duncte123.skybot.commands.uncategorized

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import ml.duncte123.skybot.utils.MessageUtils.sendMsg
import me.duncte123.botCommons.web.WebUtils
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.jsoup.Jsoup
import java.lang.management.ManagementFactory
import java.time.temporal.ChronoUnit

class OneLinerCommands : Command() {

    init {
        this.displayAliasesInHelp = true
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        when (invoke) {
            "ping" -> {
                MessageUtils.sendMsg(event, "PONG!") {
                    it.editMessage("PONG!\n" +
                            "Rest ping: ${event.message.creationTime.until(it.creationTime, ChronoUnit.MILLIS)}ms\n" +
                            "Websocket ping: ${event.jda.ping}ms\n" +
                            "Average shard ping: ${event.jda.asBot().shardManager.averagePing}ms").queue()
                }
            }

            "cookie" -> MessageUtils.sendMsg(event, "<:blobnomcookie_secret:317636549342789632>")

            "trigger" -> MessageUtils.sendEmbed(event, EmbedUtils.embedImage("https://cdn.discordapp.com/attachments/94831883505905664/176181155467493377/triggered.gif"))

            "wam" -> MessageUtils.sendEmbed(event, EmbedUtils.embedField("GET YOUR WAM NOW!!!!", "[http://downloadmorewam.com/](http://downloadmorewam.com/)"))

            "mineh" -> MessageUtils.sendMsg(event, MessageBuilder().setTTS(true).append("Insert creepy music here").build()) {
                sendEmbed(event,
                        EmbedUtils.embedImage("https://cdn.discordapp.com/attachments/204540634478936064/213983832087592960/20160813133415_1.jpg")
                )
            }


        // "event.jda.selfUser.id" might be invalid "jda.asBot().getApplicationInfo().complete().id"
            "invite" -> MessageUtils.sendMsg(event, "Invite me with this link:\n<https://discordapp.com/oauth2/authorize?client_id=${event.jda.selfUser.id}&scope=bot&permissions=8>")

            "uptime" -> MessageUtils.sendMsg(event, AirUtils.getUptime(ManagementFactory.getRuntimeMXBean().uptime, true))

            "quote" -> WebUtils.ins.getText("http://inspirobot.me/api?generate=true").async {
                sendEmbed(event, EmbedUtils.embedImage(it))
            }
            "yesno" -> {
                WebUtils.ins.getJSONObject("https://yesno.wtf/api").async {
                    sendEmbed(event, EmbedUtils.defaultEmbed()
                            .setTitle(it.getString("answer"))
                            .setImage(it.getString("image"))
                            .build())
                }

            }
        //db!eval "```${"screenfetch -N".execute().text.replaceAll("`", "​'").replaceAll("\u001B\\[[;\\d]*m", "")}```"
            "donate" -> {
                val amount = if (args.isNotEmpty()) "/" + args.joinToString(separator = "") else ""
                sendMsg(event, """Hey there thank you for your interest in supporting the bot.
                    |You can use one of the following methods to donate:
                    |**PayPal:** <https://paypal.me/duncte123$amount>
                    |**Patreon:** <https://patreon.com/duncte123>
                    |
                    |All donations are going directly into development of the bot ❤
                """.trimMargin())
            }
        //"screenfetch" -> {
        //
        //}
            "insta" -> {
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
                                .setImage(img.getString("url")).build())
                    }
                }
            }
            "xkcd" -> {
                WebUtils.ins.scrapeWebPage("https://c.xkcd.com/random/comic/").async {
                    MessageUtils.sendMsg(event, "https:" + it.select("#comic img").attr("src"))
                }
            }
            else -> println("Invoke was invalid: $invoke")
        }
    }

    override fun help(invoke: String?): String {

        return when (invoke) {
            "cookie" -> {
                """blobnomcookie
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "trigger" -> {
                """Use when you are triggered.
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "wam" -> {
                """you need more WAM!
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "mineh" -> {
                """HERE COMES MINEH!
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "invite" -> {
                """Gives you the bot invite
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "uptime" -> {
                """Shows the bot uptime
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "quote" -> {
                """Shows an inspiring quote
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "yesno" -> {
                """Chooses between yes or no
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "donate" -> {
                """Gives you a link to donate for the bot
                    |Usage: `$PREFIX$invoke [amount]`
                """.trimMargin()
            }
            "insta" -> {
                """Get the latest picture of someones profile
                    |Usage: `$PREFIX$invoke [username]`
                """.trimMargin()
            }
            "xkcd" -> """Get a random comic from xkcd.com
                |Usage: `$PREFIX$invoke`
            """.trimMargin()
            else -> "invalid invoke"
        }
    }

    override fun help() = """`${PREFIX}ping` => Shows the delay from the bot to the discord servers.
            |`${PREFIX}cookie` => blobnomcookie.
            |`${PREFIX}trigger` => Use when you are triggered.
            |`${PREFIX}wam` => You need more WAM!.
            |`${PREFIX}mineh` => HERE COMES MINEH!
            |`${PREFIX}invite` => Gives you the bot invite
            |`${PREFIX}uptime` => Shows the bot uptime
            |`${PREFIX}quote` => Shows an inspiring quote
            |`${PREFIX}yesno` => Chooses between yes or no
            |`${PREFIX}donate [amount]` => Gives you a link to donate for the bot
            |`${PREFIX}insta [amount]` => Get the latest picture of someones profile
            |`${PREFIX}xkcd` => Get a random comic from xkcd.com
    """.trimMargin()

    override fun getName() = "ping"

    override fun getAliases() = arrayOf("cookie", "trigger", "wam", "mineh", "invite", "uptime", "quote", "yesno",
            "insta", "donate", "insta", "xkcd")
}
