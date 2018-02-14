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
import ml.duncte123.skybot.utils.*
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import ml.duncte123.skybot.utils.MessageUtils.sendMsg
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

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

                MessageUtils.sendMsg(event, "PONG!") {
                    it.editMessage("PONG!\n" +
                            "Message ping is: ${System.currentTimeMillis() - time}ms\n" +
                            "Websocket ping: ${event.jda.ping}ms\n" +
                            "Average shard ping: ${event.jda.asBot().shardManager.averagePing}ms$avg").queue()
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

            "quote" -> sendEmbed(event, EmbedUtils.embedImage(WebUtils.getText("http://inspirobot.me/api?generate=true")))

            "yesno" -> {
                val json = WebUtils.getJSONObject("https://yesno.wtf/api")
                sendEmbed(event, EmbedUtils.defaultEmbed()
                        .setTitle(json.getString("answer"))
                        .setImage(json.getString("image"))
                        .build())
            }
            "kickme" -> {
                val warningMsg = """**WARNING** this command will kick you from this server
                        |If you are sure that you want to kick yourself off this server use `${PREFIX}kickme YESIMSURE`
                        |By running `${PREFIX}kickme YESIMSURE` you agree that you are responsible for the consequences of this command.
                        |DuncteBot and any of it's developers are not responsible for your own kick by running this command
                    """.trimMargin()
                if(args.isEmpty() || args[0] != "YESIMSURE") {
                    sendMsg(event, warningMsg)
                } else if(!args.isEmpty() && args[0] == "YESIMSURE") {
                    //Check for perms
                    if(event.guild.selfMember.canInteract(event.member) && event.guild.selfMember.hasPermission(Permission.KICK_MEMBERS)) {
                        MessageUtils.sendSuccess(event.message)
                        //Kick the user
                        sendMsg(event, "Your kick will commerce in 20 seconds") {
                            it.guild.controller.kick(event.member)
                                    .reason("${String.format("%#s", event.author)} ran the kickme command and got kicked")
                                    .queueAfter(20L, TimeUnit.SECONDS) {
                                        ModerationUtils.modLog(event.jda.selfUser,
                                                event.author, "kicked", "Used the kickme command", event.guild)
                                    }
                        }
                    } else {
                        sendMsg(event, """I'm missing the permission to kick you.
                            |You got lucky this time ${event.member.asMention}.
                        """.trimMargin())
                    }
                } else {
                    sendMsg(event, warningMsg)
                }
            }
            else -> println("Invoke was invalid: $invoke")
        }
    }

    override fun help(invoke: String?): String {

        return when(invoke) {
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
            "kickme" -> {
                """Kickes you off the server
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            else -> {
                "invalid invoke"
            }
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
            |`${PREFIX}kickme` => Kicks you off the server
    """.trimMargin()

    override fun getName() = "ping"
    
    override fun getAliases() = arrayOf("cookie", "trigger", "wam", "mineh", "invite", "uptime", "quote", "yesno", "kickme")

    private fun getAverage(): Double = pingHistory.filter { it != -1L }.map { it.toDouble() }.average()
}
