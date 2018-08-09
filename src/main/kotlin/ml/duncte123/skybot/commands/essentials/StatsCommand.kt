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

package ml.duncte123.skybot.commands.essentials

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.EmbedUtils.defaultEmbed
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed

class StatsCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {

        val embed = defaultEmbed()

                .addField("Discord/bot Stats",
                  """**Guilds:**
                    |**Users (unique):**
                    |**Text channels:**
                    |**Voice channels:**
                    |~~**Emotes:**~~
                    |**Playing music count:**
                    |**Uptime:**
                """.trimMargin(), false)

                .addField("Server stats",
                  """**CPU's:**
                    |**CPU usage:**
                    |**Total ram:**
                    |**Ram usage:**
                    |**System uptime:**
                """.trimMargin(), false)

                .addField("JVM stats",
                          """**Total thread count:**
                            |**Active thread count:**
                        """.trimMargin(), false)

        sendEmbed(ctx.event, embed.build())

    }

    override fun getName() = "stats"

    override fun help() = "Shows some nerdy stats about the bot"

    override fun getCategory() = CommandCategory.NERD_STUFF
}