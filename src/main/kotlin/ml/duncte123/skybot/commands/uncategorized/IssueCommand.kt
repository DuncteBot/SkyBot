/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import io.sentry.Sentry
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.Permission

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class IssueCommand : Command() {

    val regex = "\\s+".toRegex()

    init {
        this.name = "issue"
        this.aliases = arrayOf("bug", "bugreport")
        this.helpFunction = { _, _ ->
            """Reports heavy and weird issues to the developers.
        |This will create an invite to your server, so we can join and help you directly.
        |Those issues are hard to explain / resolve if we can't see nor read the chat or other things that happen.
    """.trimMargin()
        }
        this.usageInstructions = { prefix, invoke -> "`$prefix$invoke <issue json>` (issue can be generated at https://dunctebot.com/issuegenerator" }
    }

    override fun execute(ctx: CommandContext) {
        val event = ctx.event

        when (ctx.args.size) {
            0 -> {
                sendErrorWithMessage(event.message, """Well you forgot to add formatted data we require so we can resolve the issue faster.
                    |You can generate it by using our dashboard. Link: <https://dunctebot.com/issuegenerator>""".trimMargin())
            }
            else -> {
                try {
                    val issue = ctx.variables.jackson.readValue(ctx.argsRaw, Issue::class.java)
                    val cmds = issue.lastCommands.map {
                        if (it.contains(regex)) {
                            val split = it.split(regex)
                            return@map split[0] + " " + split.takeLastWhile { s -> split.indexOf(s) != 0 }.joinToString(separator = " ", transform = { "<$it>" })
                        } else {
                            return@map it
                        }
                    }.joinToString(", ")

                    var invite = issue.inv

                    if (issue.inv.isNullOrEmpty() && ctx.selfMember.hasPermission(ctx.channel, Permission.CREATE_INSTANT_INVITE)) {
                        invite = event.channel.createInvite().complete().url
                    }

                    val embed = EmbedUtils.defaultEmbed()
                        .setTitle("Issue by ${String.format("%#s / %s", event.author, event.author.id)}")
                        .setFooter(null, null)
                        .setDescription("""
                            |Description: ${issue.description}
                            |Detailed report: ${issue.detailedReport}
                            """.trimMargin())
                        .addField("Invite:", invite, false)
                        .addField("List of recent run commands:", cmds, false)

                    sendEmbed(ctx.shardManager.getTextChannelById(424146177626210305L), embed)

                    sendMsg(ctx, "Issue submitted, we suggest that you join our server so that we can contact you easier if you haven't already.\n" +
                        "https://discord.gg/NKM9Xtk")
                } catch (ex: Exception) {
                    Sentry.capture(ex)

                    val msg = """You malformed the JSON.
                            | Expected pattern: {"lastCommands": ["help", "join"],"description": "","detailedReport": "", "inv": "discord.gg/asdfsa"}"""

                    sendErrorWithMessage(event.message, msg.trimMargin())
                }
            }
        }
    }
}

class Issue {
    lateinit var lastCommands: List<String>
    lateinit var description: String
    lateinit var detailedReport: String
    var inv: String? = null
}
