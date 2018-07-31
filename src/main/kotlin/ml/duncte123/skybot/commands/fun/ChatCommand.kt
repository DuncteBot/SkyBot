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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botCommons.web.WebUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import ml.duncte123.skybot.utils.Variables
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


class ChatCommand : Command() {

    private val botid = "b0dafd24ee35a477"
    private val sessions = TreeMap<Long, ChatSession>()
    private val MAX_DURATION = MILLISECONDS.convert(20, MINUTES)
    private val responses = arrayOf(
            "My prefix in this guild is *`{PREFIX}`*",
            "Thanks for asking, my prefix here is *`{PREFIX}`*",
            "That should be *`{PREFIX}`*",
            "It was *`{PREFIX}`* if I'm not mistaken",
            "In this server my prefix is *`{PREFIX}`*"
    )

    init {
        this.category = CommandCategory.FUN

        commandService.scheduleAtFixedRate({
            val temp = TreeMap<Long, ChatSession>(sessions)
            val now = Date()
            var cleared = 0
            temp.forEach {
                val duration = now.time - it.value.time.time
                if (duration >= MAX_DURATION) {
                    sessions.remove(it.key)
                    cleared++
                }
            }
            logger.debug("Removed $cleared chat sessions that have been inactive for 20 minutes.")
        }, 1L, 1L, TimeUnit.HOURS)
    }


    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (event.message.contentRaw.contains("prefix")) {
            MessageUtils.sendMsg(event, "${event.author.asMention}, " + responses[Variables.RAND.nextInt(responses.size)]
                    .replace("{PREFIX}", getSettings(event.guild).customPrefix))
            return
        }

//        if (!hasUpvoted(event.author)) {
//            MessageUtils.sendEmbed(event, EmbedUtils.embedMessage(
//                    "I'm sorry but you can't use the chat feature because you haven't up-voted the bot." +
//                            " You can up-vote the bot and get access to this feature [here](https://discordbots.org/bot/210363111729790977" +
//                            ") or become a patreon [here](https://patreon.com/duncte123)"))
//            return
//        }

        if (args.isEmpty()) {
            MessageUtils.sendMsg(event, "Incorrect usage: `$PREFIX$name <message>`")
            return
        }
        val time = System.currentTimeMillis()
        var message = event.message.contentRaw.split("\\s+".toRegex(), 2)[1]
        event.channel.sendTyping().queue()

        message = replaceStuff(event, message)

        if (!sessions.containsKey(event.author.idLong)) {
            sessions[event.author.idLong] = ChatSession(botid, event.author.idLong)
            //sessions[event.author.id]?.session =
        }
        logger.debug("Message: \"$message\"")
        //Set the current date in the object
        sessions[event.author.idLong]!!.time = Date()

        sessions[event.author.idLong]!!.think(message) {
            var response = it

            val withAds = Variables.RAND.nextInt(1000) in 211 until 268 && !hasUpvoted(event.author)

            response = parseATags(response, withAds)
            if (withAds) {
                response += "\n\nHelp supporting our bot by upvoting [here](https://discordbots.org/bot/210363111729790977) " +
                        "or becoming a patron [here](https://patreon.com/duncte123)."
                MessageUtils.sendMsg(event, MessageBuilder().append(event.author).setEmbed(EmbedUtils.embedMessage(response)).build())
            } else {
                MessageUtils.sendMsg(event, "${event.author.asMention}, $response")
            }
            logger.debug("New response: \"$response\", this took ${System.currentTimeMillis() - time}ms")
        }

    }

    private fun parseATags(response: String, withAds: Boolean): String {
        var response1 = response
        for (element in Jsoup.parse(response1).getElementsByTag("a")) {
            response1 = response1.replace(oldValue = element.toString(),
                    newValue = if (withAds) "[${element.text()}](${element.attr("href")})" else
                    //It's usefull to show the text
                        "${element.text()}(<${element.attr("href")}>)")
        }
        return response1
    }

    private fun replaceStuff(event: GuildMessageReceivedEvent, m: String): String {
        //We don't need this because we are using contentDisplay instead of contentRaw
        //We need it since contentDisplay leaves # and @
        var message = m
        for (it in event.message.mentionedChannels) {
            message = message.replace(it.asMention, it.name)
        }
        for (it in event.message.mentionedRoles) {
            message = message.replace(it.asMention, it.name)
        }
        for (it in event.message.mentionedUsers) {
            message = message.replace(it.asMention, it.name)
        }
        for (it in event.message.emotes) {
            message = message.replace(it.asMention, it.name)
        }
        message = message.replace("@here", "here").replace("@everyone", "everyone")
        return message
    }

    override fun help() = "Have a chat with dunctebot\n" +
            "Usage: `$PREFIX$name <message>`"

    override fun getName() = "chat"
}

/**
 * Little wrapper class to help us keep track of inactive sessions
 */
class ChatSession(botid: String, userId: Long) {
    private val vars: MutableMap<String, Any>

    init {
        vars = LinkedHashMap()
        vars["botid"] = botid
        vars["custid"] = userId
    }

    var time = Date()

    fun think(text: String, response: (String) -> Unit) {
        vars["input"] = URLEncoder.encode(text, "UTF-8")
        WebUtils.ins.preparePost("https://www.pandorabots.com/pandora/talk-xml", vars).async {
            response.invoke(xPathSearch(it, "//result/that/text()"))
        }
    }

    private fun xPathSearch(input: String, expression: String): String {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val xPath = XPathFactory.newInstance().newXPath()
        val xPathExpression = xPath.compile(expression)
        val document = documentBuilder.parse(ByteArrayInputStream(input.toByteArray(charset("UTF-8"))))
        val output = xPathExpression.evaluate(document, XPathConstants.STRING) as String
        return output.trim { it <= ' ' }
    }

}