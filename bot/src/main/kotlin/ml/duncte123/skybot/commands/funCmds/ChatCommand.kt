/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.funCmds

import gnu.trove.map.hash.TLongObjectHashMap
import me.duncte123.botcommons.messaging.MessageConfig
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.requests.FormRequestBody
import ml.duncte123.skybot.Settings.NO_STATIC
import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.MapUtils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.jsoup.Jsoup
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class ChatCommand : Command() {
    private val sessions = MapUtils.newLongObjectMap<ChatSession>()
    private val maxDuration = MILLISECONDS.convert(20, MINUTES)
    private val responses = arrayOf(
        "My prefix in this guild is *`{PREFIX}`*",
        "Thanks for asking, my prefix here is *`{PREFIX}`*",
        "That should be *`{PREFIX}`*",
        "It was *`{PREFIX}`* if I'm not mistaken",
        "In this server my prefix is *`{PREFIX}`*"
    )

    init {
        this.category = CommandCategory.FUN
        this.name = "chat"
        this.help = "Have a chat with DuncteBot"
        this.usage = "<message>"
        // needed for reactions
        this.botPermissions = arrayOf(
            Permission.MESSAGE_HISTORY
        )

        SkyBot.SYSTEM_POOL.scheduleAtFixedRate(
            {
                AirUtils.runOnVirtual(this::doCleanup)
            },
            1L, 1L, TimeUnit.HOURS
        )
    }

    override fun execute(ctx: CommandContext) {
        val event = ctx.event
        ctx.channel.sendTyping().queue()

        if (ctx.message.contentRaw.lowercase().contains("prefix")) {
            sendMsg(
                MessageConfig.Builder.fromCtx(ctx)
                    .replyTo(ctx.message)
                    .setMessage(responses.random().replace("{PREFIX}", ctx.prefix))
            )
            return
        }

        if (ctx.args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val time = System.currentTimeMillis()
        var message = ctx.argsJoined

        message = replaceMentionsWithText(event, message)

        if (!sessions.containsKey(event.author.idLong)) {
            sessions.put(event.author.idLong, ChatSession(event.author.idLong))
            // sessions[event.author.id]?.session =
        }

        val session = sessions[event.author.idLong] ?: return

        LOGGER.debug("Message: \"$message\"")

        // Set the current date in the object
        session.time = Date()

        session.think(message) {
            val response = parseATags(it)

            LOGGER.debug("New response: \"$response\", this took ${System.currentTimeMillis() - time}ms")

            if (response.isEmpty() || response.isBlank()) {
                sendMsg(
                    MessageConfig.Builder.fromCtx(ctx)
                        .replyTo(ctx.message)
                        .setMessage(
                            "$NO_STATIC Chatbot error: no content returned, " +
                                "this is likely due to the chatbot banning you (we are working on a fix)"
                        )
                )
                return@think
            }

            sendMsg(
                MessageConfig.Builder.fromCtx(ctx)
                    .replyTo(ctx.message)
                    .setMessage(response)
            )
        }
    }

    private fun parseATags(response: String): String {
        var response1 = response
        for (element in Jsoup.parse(response1).getElementsByTag("a")) {
            response1 = response1.replace(
                oldValue = element.toString(),
                newValue = "${element.text()}(<${element.attr("href")}>)"
            )
        }
        return response1
    }

    private fun replaceMentionsWithText(event: MessageReceivedEvent, m: String): String {
        var message = m
        val mentions = event.message.mentions

        for (it in mentions.channels) {
            message = message.replace(it.asMention, it.name)
        }
        for (it in mentions.roles) {
            message = message.replace(it.asMention, it.name)
        }
        for (it in mentions.users) {
            message = message.replace(it.asMention, it.name)
        }
        for (it in mentions.customEmojis) {
            message = message.replace(it.asMention, it.name)
        }
        message = message.replace("@here", "here").replace("@everyone", "everyone")
        return message
    }

    private fun doCleanup() {
        val temp = TLongObjectHashMap(sessions)
        val now = Date()
        var cleared = 0
        for (it in temp.keys()) {
            val duration = now.time - sessions.get(it).time.time
            if (duration >= maxDuration) {
                sessions.remove(it)
                cleared++
            }
        }
        LOGGER.debug("Removed $cleared chat sessions that have been inactive for 20 minutes.")
    }
}

class ChatSession(userId: Long) {
    private val body = FormRequestBody()

    init {
        body.append("botid", "b0dafd24ee35a477")
        body.append("custid", userId.toString())
    }

    var time = Date()

    fun think(text: String, response: (String) -> Unit) {
        body.append("input", text)
        WebUtils.ins.postRequest("https://www.pandorabots.com/pandora/talk-xml", body)
            .build({ it.body!!.byteStream() }, WebParserUtils::handleError)
            .async {
                try {
                    response.invoke(xPathSearch(it, "//result/that/text()"))
                } catch (e: Exception) {
                    response.invoke(
                        """An Error occurred, please report this message my developers
                    |```
                    |${e.message}
                    |```
                        """.trimMargin()
                    )
                }
            }
    }

    @Suppress("SameParameterValue")
    private fun xPathSearch(input: InputStream, expression: String): String {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val xPath = XPathFactory.newInstance().newXPath()
        val xPathExpression = xPath.compile(expression)

        return input.use {
            val document = documentBuilder.parse(it)
            val output = xPathExpression.evaluate(document, XPathConstants.STRING)
            val outputString = (output as? String) ?: "Error on xpath, this should never be shown"

            return@use outputString.trim { s -> s <= ' ' }
        }
    }
}
