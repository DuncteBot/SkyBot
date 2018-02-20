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

@file:Suppress("MemberVisibilityCanPrivate")

package ml.duncte123.skybot.commands.`fun`

import com.google.code.chatterbotapi.ChatterBot
import com.google.code.chatterbotapi.ChatterBotFactory
import com.google.code.chatterbotapi.ChatterBotSession
import com.google.code.chatterbotapi.ChatterBotType
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.jsoup.Jsoup

class ChatCommand : Command() {

    private val builder: ChatterBot
    private var oldBot: ChatterBotSession
    private val responses = arrayOf(
        "My PREFIX in this guild is *`{PREFIX}`*",
        "Thanks for asking, my PREFIX here is *`{PREFIX}`*",
        "That should be *`{PREFIX}`*",
        "It was *`{PREFIX}`* if I'm not mistaken"
    )

    init {
        this.category = CommandCategory.FUN
        logger.info("Starting AI")
        //New chat Bot :D
        builder = ChatterBotFactory()
                .create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477")
        oldBot = builder.createSession()

        logger.info("AI has been loaded.")
    }


    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        if(args.isEmpty()){
            MessageUtils.sendMsg(event, "Incorrect usage: `$PREFIX$name <message>`")
            return
        }
        val time = System.currentTimeMillis()
        var message = event.message.contentRaw.split( "\\s+".toRegex(),2)[1]
        event.channel.sendTyping().queue()

        if(event.message.contentRaw.contains("prefix")) {
            MessageUtils.sendMsg(event, "${event.author.asMention}, " + responses[AirUtils.RAND.nextInt(responses.size)]
                    .replace("{PREFIX}", getSettings(event.guild).customPrefix))
            return
        }

//        sendMsg(event, "The chat feature has temporally been disabled due to lag issues with the music.\n" +
//                "We don't know if we ever will bring it back, thank you for understanding.")

        //We don't need this because we are using contentDisplay instead of contentRaw
        //We need it since contentDisplay leaves # and @
        event.message.mentionedChannels.forEach { message = message.replace(it.asMention, it.name) }
        event.message.mentionedRoles.forEach { message = message.replace(it.asMention, it.name) }
        event.message.mentionedUsers.forEach { message = message.replace(it.asMention, it.name) }
        event.message.emotes.forEach { message = message.replace(it.asMention, it.name) }
        message = message.replace("@here", "here").replace("@everyone", "everyone")

        logger.debug("Message: \"$message\"")
        var response = oldBot.think(message)

        //Reset the ai if it dies
        if(response == "You have been banned from talking to me.") {
            this.resetAi()
            response = oldBot.think(message)
        }

        for (element in Jsoup.parse(response).getElementsByTag("a")) {
            response = response.replace(oldValue = element.toString(), newValue = "<${element.attr("href")}>")
        }
        MessageUtils.sendMsg(event, "${event.author.asMention}, $response")
        logger.debug("New response: \"$response\", this took ${System.currentTimeMillis() - time}ms")
    }

    override fun help() = "Have a chat with dunctebot\n" +
            "Usage: `$PREFIX$name <message>`"

    override fun getName() = "chat"

    fun resetAi() {
        oldBot = builder.createSession()
    }
}