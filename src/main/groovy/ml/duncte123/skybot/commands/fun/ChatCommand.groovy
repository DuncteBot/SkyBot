/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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

package ml.duncte123.skybot.commands.fun

import ml.duncte123.skybot.objects.chatai.AI
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils
import org.slf4j.event.Level

class ChatCommand extends Command {

    private AI ai
    def responses = [
            "My prefix in this guild is {PREFIX}",
            "Thanks for asking, my prefix here is {PREFIX}",
            "That should be {PREFIX}",
            "It was {PREFIX} if I'm not mistaken"
    ]

    ChatCommand() {
        AirUtils.log(Level.INFO, "Starting AI")
        this.category = CommandCategory.NERD_STUFF
        this.ai = new AI(AirUtils.config.getString("apis.cleverbot.user"), AirUtils.config.getString("apis.cleverbot.api"))
                //Use the current milliseconds to get a available username every login
                .setNick(Settings.defaultName + System.currentTimeMillis())
                .create({
                    AirUtils.log(Level.INFO, "AI has been loaded, server response: ${it.toString()}")
                })
    }

    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if(args.length < 1){
            sendMsg(event, "Incorrect usage: `$PREFIX$name <message>`")
            return
        }
        def time = System.currentTimeMillis()
        def message = StringUtils.join(args, " ")
        event.channel.sendTyping().queue()
        AirUtils.log(Level.DEBUG, "New Question: $message")
        if(message.contains("prefix")) {
            sendMsg(event, "${event.author.asMention}, " + responses.get(AirUtils.rand.nextInt(responses.size()))
                    .replace("{PREFIX}", "`${getSettings(event.guild).customPrefix}`"))
            return
        }
        ai.ask(message, { json ->
            AirUtils.log(Level.DEBUG, "New response: ${json.toString()}, this took ${System.currentTimeMillis() - time}ms")
            if(json["status"] == "success") {
                sendMsg(event, "${event.author.asMention}, ${json["response"]}")
            } else {
                sendMsg(event, "Error: ${json["response"]}")
            }
        })
    }

    @Override
    String help() {
        return "Have a chat with dunctebot\n" +
                "Usage: `$PREFIX$name <message>`"
    }

    @Override
    String getName() {
        return "chat"
    }
}
