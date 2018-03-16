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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.commands.uncategorized

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class IssueCommand : Command() {
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        when (args.size) {
            0 -> {
                MessageUtils.sendErrorWithMessage(event.message, """Well you forgot to add formatted data we require so we can resolve it faster.
                    |You can generate it by using our dashboard. Link: <https://bot.duncte123.me>""".trimMargin())
            }
        }
    }

    override fun help(): String = """Reports heavy and wierd issues to the developers.
        |This will create an invite to your server, so we can join and help you directly.
        |Those issues are hard to explain / resolve if we can't see nor read the chat other things that happen.
    """.trimMargin()

    override fun getName(): String = "issue"
}