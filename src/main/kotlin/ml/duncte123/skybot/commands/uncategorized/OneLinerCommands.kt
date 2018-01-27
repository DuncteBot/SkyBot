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

import ml.duncte123.skybot.entities.SizedList
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import ml.duncte123.skybot.utils.WebUtils
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.lang.management.ManagementFactory

class OneLinerCommands : Command() {

    companion object {
        @JvmStatic
        val pingHistory: SizedList<Long> = SizedList(25)
    }
    
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        when (invoke) {
            "ping" -> {
                val time = System.currentTimeMillis()
                val avg = if (!getAverage().isNaN()) "\nAverage music ping: ${getAverage()}ms" else ""

                event.channel.sendMessage("PONG!").queue {
                    it.editMessage("PONG!\n" +
                            "Message ping is: ${System.currentTimeMillis() - time}ms\n" +
                            "Websocket ping: ${event.jda.ping}ms\n" +
                            "Average shard ping: ${event.jda.asBot().shardManager.averagePing}ms$avg").queue()
                }
            }

            "cookie" -> MessageUtils.sendMsg(event, "<:blobnomcookie_secret:317636549342789632>")

            "trigger" -> MessageUtils.sendEmbed(event, EmbedUtils.embedImage("https://cdn.discordapp.com/attachments/94831883505905664/176181155467493377/triggered.gif"))

            "wam" -> MessageUtils.sendEmbed(event, EmbedUtils.embedField("GET YOUR WAM NOW!!!!", "[http://downloadmorewam.com/](http://downloadmorewam.com/)"))

            "mineh" -> event.channel.sendMessage(MessageBuilder().setTTS(true).append("Insert creepy music here").build())
                    .queue { MessageUtils.sendEmbed(event, EmbedUtils.embedImage("https://cdn.discordapp.com/attachments/204540634478936064/213983832087592960/20160813133415_1.jpg")) }


            // "event.jda.selfUser.id" might be invalid "jda.asBot().getApplicationInfo().complete().id"
            "invite" -> MessageUtils.sendMsg(event, "Invite me with this link:\n<https://discordapp.com/oauth2/authorize?client_id=${event.jda.selfUser.id}&scope=bot&permissions=8>")

            "uptime" -> MessageUtils.sendMsg(event, AirUtils.getUptime(ManagementFactory.getRuntimeMXBean().uptime, true))

            "quote" -> sendEmbed(event, EmbedUtils.embedImage(WebUtils.getText("http://inspirobot.me/api?generate=true")))

            "yesno" -> {
                val json = WebUtils.getJSONObject("https://yesno.wtf/api")
                sendEmbed(event, EmbedUtils.defaultEmbed()
                        .setTitle(json.getString("answer"))
                        .setImage(json.getString("image"))
                        .build())
            }
            else -> println("Invoke was invalid: $invoke")
        }
    }
    override fun help() = """`${PREFIX}ping` => Shows the delay from the bot to the discord servers.
            |`${PREFIX}cookie` => blobnomcookie.
            |`${PREFIX}trigger` => use when you are triggered.
            |`${PREFIX}wam` => you need more WAM!.
            |`${PREFIX}mineh` => HERE COMES MINEH!
            |`${PREFIX}invite` => gives you the bot invite
            |`${PREFIX}uptime` => shows the bot uptime
            |`${PREFIX}quote` => Shows an inspiring quote""".trimMargin()

    override fun getName() = "ping"
    
    override fun getAliases() = arrayOf("cookie", "trigger", "wam", "mineh", "invite", "uptime", "quote", "yesno")

    private fun getAverage(): Double = pingHistory.filter { it != -1L }.map { it.toDouble() }.average()
}
