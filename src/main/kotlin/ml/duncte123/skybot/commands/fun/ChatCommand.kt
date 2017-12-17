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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.`fun`

import com.batiaev.aiml.bot.BotImpl
import com.batiaev.aiml.chat.Chat
import com.batiaev.aiml.chat.ChatContext
import com.google.code.chatterbotapi.ChatterBot
import com.google.code.chatterbotapi.ChatterBotFactory
import com.google.code.chatterbotapi.ChatterBotSession
import com.google.code.chatterbotapi.ChatterBotType
import ml.duncte123.skybot.entities.DiscordChannel
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AiUtils
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.jsoup.Jsoup
import org.slf4j.event.Level

class ChatCommand : Command() {

    //private val builder: ChatterBot
    //private var oldBot: ChatterBotSession
    private val bot: BotImpl
    private val context = ChatContext(Settings.defaultName)
    private val responses = arrayOf(
        "My prefix in this guild is {PREFIX}",
        "Thanks for asking, my prefix here is {PREFIX}",
        "That should be {PREFIX}",
        "It was {PREFIX} if I'm not mistaken"
    )

    init {
        this.category = CommandCategory.FUN
        AirUtils.log("ChatCommand", Level.INFO, "Starting AI")
        //New chat Bot :D
        //builder = ChatterBotFactory()
        //        .create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477")
        //oldBot = builder.createSession()

        bot = AiUtils.get(Settings.defaultName.toLowerCase())

        AirUtils.log("ChatCommand", Level.INFO, "AI has been loaded.")
    }


    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        if(!bot.wakeUp()) {
            sendError(event.message)
            sendMsg(event, "The chat is not available atm, try again later")
            return
        }

        if(args.isEmpty()){
            sendMsg(event, "Incorrect usage: `$PREFIX$name <message>`")
            return
        }
        val time = System.currentTimeMillis()
        val message = event.message.contentDisplay.split( "\\s+".toRegex(),2)[1]
        event.channel.sendTyping().queue()

        if(message.contains("prefix")) {
            sendMsg(event, "${event.author.asMention}, " + responses[AirUtils.rand.nextInt(responses.size)]
                    .replace("{PREFIX}", "`${getSettings(event.guild).customPrefix}`"))
            return
        }

        event.message.mentionedChannels.forEach { message.replace(it.asMention, it.name) }
        event.message.mentionedRoles.forEach { message.replace(it.asMention, it.name) }
        event.message.mentionedUsers.forEach { message.replace(it.asMention, it.name) }
        event.message.emotes.forEach { message.replace(it.asMention, it.name) }
        message.replace("@here", "here").replace("@everyone", "everyone")

        //var response = oldBot.think(message)
        var response = bot.multisentenceRespond(message, context)
        this.context.newState(message, response)
        if (response.startsWith(prefix = "<")) {
            response = """<${Jsoup.parse(response.substring(response.indexOfFirst { it == '<'}..(response.indexOfLast { it == '>' } + 1)))
                    .getElementsByTag("a").first().attr("href")}>${response.subSequence((response.indexOfLast { it == '>' } + 1)..(response.length - 1))}"""
        }
        sendMsg(event, "${event.author.asMention}, $response")
        AirUtils.log(Level.DEBUG, "New response: \"$response\", this took ${System.currentTimeMillis() - time}ms")
    }

    override fun help() = "Have a chat with dunctebot\n" +
            "Usage: `$PREFIX$name <message>`"

    override fun getName() = "chat"

    fun resetAi() {
        //oldBot = builder.createSession()
    }
}