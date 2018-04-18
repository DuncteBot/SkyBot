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

import com.google.code.chatterbotapi.ChatterBotSession
import com.google.code.chatterbotapi.ChatterBotThought
import kotlinx.coroutines.experimental.async
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.jsoup.Jsoup
import java.io.*
import java.net.HttpCookie
import java.net.HttpURLConnection
import java.net.URL
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
    //private var oldBot: ChatterBotSession
    private val sessions = TreeMap<String, ChatSession>()
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
            val temp = TreeMap<String, ChatSession>(sessions)
            val now = Date()
            val MAX_DURATION = MILLISECONDS.convert(20, MINUTES)
            var cleared = 0
            temp.forEach {
                val duration = now.time - it.value.time.time
                if (duration >= MAX_DURATION) {
                    sessions.remove(it.key)
                    cleared++
                }
            }
            logger.info("Removed $cleared chat sessions that have been inactive for 20 minutes.")
        }, 30L, 30L, TimeUnit.MINUTES)
    }


    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (event.message.contentRaw.contains("prefix")) {
            MessageUtils.sendMsg(event, "${event.author.asMention}, " + responses[AirUtils.RAND.nextInt(responses.size)]
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

        //We don't need this because we are using contentDisplay instead of contentRaw
        //We need it since contentDisplay leaves # and @
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

        if(!sessions.containsKey(event.author.id)) {
            sessions[event.author.id] = ChatSession(botid, event.author.id)
            //sessions[event.author.id]?.session =
        }

        async {
            logger.debug("Message: \"$message\"")
            //Set the current date in the object
            sessions[event.author.id]?.time = Date()

            var response = sessions[event.author.id]?.think(message)

            //Reset the ai if it dies
            //But not for now to see how user separated sessions go
            /*if (response == "" || response == "You have been banned from talking to me." ||
                    response == "I am not talking to you any more.") {
                sessions[event.author.id]?.session = builder.createSession()
                response = sessions[event.author.id]?.session?.think(message)
            }*/

            val `with"Ads"` = AirUtils.RAND.nextInt(500) in 211 until 268 && !hasUpvoted(event.author)

            for (element in Jsoup.parse(response).getElementsByTag("a")) {
                response = response?.replace(oldValue = element.toString(),
                        newValue = if (`with"Ads"`) "[${element.text()}](${element.attr("href")})" else "<${element.attr("href")}>")
            }
            response = response?.replace("Chomsky", "DuncteBot")
            if (`with"Ads"`) {
                response += "\n\nHelp supporting our bot by upvoting [here](https://discordbots.org/bot/210363111729790977) " +
                        "or becoming a patron [here](https://patreon.com/duncte123)."
                MessageUtils.sendMsg(event, MessageBuilder().append(event.author).setEmbed(EmbedUtils.embedMessage(response)).build())
            } else {
                MessageUtils.sendMsg(event, "${event.author.asMention}, $response")
            }
            logger.debug("New response: \"$response\", this took ${System.currentTimeMillis() - time}ms")
        }

    }

    override fun help() = "Have a chat with dunctebot\n" +
            "Usage: `$PREFIX$name <message>`"

    override fun getName() = "chat"
}

/**
 * Little wrapper class to help us keep track of inactive sessions
 */
class ChatSession(botid: String, userId: String) : ChatterBotSession {

    private val vars: MutableMap<String, String>

    init {
        vars = LinkedHashMap()
        vars["botid"] = botid
        vars["custid"] = userId
    }

    var time = Date()

    override fun think(thought: ChatterBotThought?): ChatterBotThought {
        vars["input"] = thought!!.text

        val response = ChatterBotUtils.request("https://www.pandorabots.com/pandora/talk-xml", null, null, vars)

        val responseThought = ChatterBotThought()

        responseThought.text = ChatterBotUtils.xPathSearch(response, "//result/that/text()")

        return responseThought
    }

    override fun think(text: String?): String {
        val thought = ChatterBotThought()
        thought.text = text
        return think(thought).text
    }
}

/*
    chatter-bot-api
    Copyright (C) 2011 pierredavidbelanger@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
internal object ChatterBotUtils {

    @Throws(Exception::class)
    private fun parametersToWWWFormURLEncoded(parameters: Map<String, String>): String {
        val s = StringBuilder()
        for ((key, value) in parameters) {
            if (s.isNotEmpty()) {
                s.append("&")
            }
            s.append(URLEncoder.encode(key, "UTF-8"))
            s.append("=")
            s.append(URLEncoder.encode(value, "UTF-8"))
        }
        return s.toString()
    }

    @Throws(Exception::class)
    fun request(url: String, headers: Map<String, String>?, cookies: MutableMap<String, String>?, parameters: Map<String, String>?): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36")
        if (headers != null) {
            for ((key, value) in headers) {
                connection.setRequestProperty(key, value)
            }
        }
        if (cookies != null && !cookies.isEmpty()) {
            val cookieHeader = StringBuilder()
            for (cookie in cookies.values) {
                if (cookieHeader.isNotEmpty()) {
                    cookieHeader.append(";")
                }
                cookieHeader.append(cookie)
            }
            connection.setRequestProperty("Cookie", cookieHeader.toString())
        }
        connection.doInput = true
        if (parameters != null && !parameters.isEmpty()) {
            connection.doOutput = true
            val osw = OutputStreamWriter(connection.outputStream)
            osw.write(parametersToWWWFormURLEncoded(parameters))
            osw.flush()
            osw.close()
        }
        if (cookies != null) {
            for (headerEntry in connection.headerFields.entries) {
                if (headerEntry.key != null && headerEntry.key.equals("Set-Cookie", ignoreCase = true)) {
                    for (header in headerEntry.value) {
                        for (httpCookie in HttpCookie.parse(header)) {
                            cookies[httpCookie.name] = httpCookie.toString()
                        }
                    }
                }
            }
        }
        val r = BufferedReader(InputStreamReader(connection.inputStream))
        val w = StringWriter()
        val buffer = CharArray(1024)
        var n = 0
        while (n != -1) {
            w.write(buffer, 0, n)
            n = r.read(buffer)
        }
        r.close()
        return w.toString()
    }

    @Throws(Exception::class)
    fun xPathSearch(input: String, expression: String): String {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val xPath = XPathFactory.newInstance().newXPath()
        val xPathExpression = xPath.compile(expression)
        val document = documentBuilder.parse(ByteArrayInputStream(input.toByteArray(charset("UTF-8"))))
        val output = xPathExpression.evaluate(document, XPathConstants.STRING) as String
        return output.trim { it <= ' ' }
    }
}