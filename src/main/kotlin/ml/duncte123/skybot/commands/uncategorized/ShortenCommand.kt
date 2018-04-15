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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.commands.uncategorized

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import ml.duncte123.skybot.utils.MessageUtils.sendMsg
import ml.duncte123.skybot.utils.WebUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class ShortenCommand : Command() {

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (args.isEmpty() || args[0].isEmpty()) {
            sendMsg(event, "Incorrect usage: `$PREFIX$name <link to shorten>`")
            return
        }

        if (!hasUpvoted(event.author)) {
            sendEmbed(event, EmbedUtils.embedMessage(
                    "You cannot use the shorten command as you haven't up-voted the bot." +
                            " You can upvote the bot [here](https://discordbots.org/bot/210363111729790977" +
                            ") or become a patreon [here](https://patreon.com/duncte123)"))
            return
        }

        if(!args[0].matches(Regex("http(s?):\\/\\/")) && !args[0].startsWith("https://") ) {
            sendMsg(event, "That does not look like a valid url")
            return
        }

        WebUtils.ins.shortenUrl(args[0]).async ({
            sendMsg(event, "Here is your shortened url: <$it>")
        }, {
            sendMsg(event, "Something went wrong, please make sure that your url to shorten is valid")
        })
    }

    override fun help(): String = """Shortens a url
            |Usage: `$PREFIX$name <link to shorten>`""".trimMargin()

    override fun getName(): String = "shorten"

    override fun getAliases(): Array<String> = arrayOf("short", "url", "bitly", "googl")
}