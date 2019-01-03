/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import org.json.JSONException
import org.json.JSONObject

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class IssueCommand : Command() {

    val regex = "\\s+".toRegex()

    @Suppress()
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        val arg = event.message.contentRaw.split(regex, 2)
        when (ctx.args.size) {
            0 -> {
                MessageUtils.sendErrorWithMessage(event.message, """Well you forgot to add formatted data we require so we can resolve it faster.
                    |You can generate it by using our dashboard. Link: <https://bot.duncte123.me/issuegenerator>""".trimMargin())
            }
            else -> {
                try {
                    val data = JSONObject(arg[1])
                    val cmds = data.getJSONArray("lastCommands").toList().map {
                        if ((it as String).contains(regex)) {
                            val split = it.split(regex)
                            return@map split[0] + " " + split.takeLastWhile { split.indexOf(it) != 0 }.joinToString(separator = " ", transform = { "<$it>" })
                        } else {
                            return@map it
                        }
                    }.joinToString(", ")
                    val embed = EmbedUtils.defaultEmbed()

                    embed.setTitle("Issue by ${String.format("%#s / %s", event.author, event.author.id)}")
                        .setFooter(null, null)
                        .setDescription("""
                            |Description: ${data.getString("description")}
                            |Detailed report: ${data.getString("detailedReport")}
                            |List of recent run commands: $cmds
                            """.trimMargin())
                        .addField("Invite:", if (data.isNull("inv") || data.getString("inv").isBlank()) event.channel.createInvite().complete(true).url else data.getString("inv"), false)

                    sendEmbed(event.jda.getTextChannelById(424146177626210305L), embed)
                } catch (ex: JSONException) {
                    val msg =
                        """You malformed the JSON.
                            | Expected pattern: {"lastCommands": ["help", "join"],"description": "","detailedReport": "", "inv": "discord.gg/abcdefh"}"""
                    MessageUtils.sendErrorWithMessage(event.message, msg.trimMargin())
                }
            }
        }
    }

    override fun help(): String = """Reports heavy and weird issues to the developers.
        |This will create an invite to your server, so we can join and help you directly.
        |Those issues are hard to explain / resolve if we can't see nor read the chat or other things that happen.
    """.trimMargin()

    override fun getName(): String = "issue"
}
