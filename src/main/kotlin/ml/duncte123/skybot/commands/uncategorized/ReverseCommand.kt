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

import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext

class ReverseCommand : Command() {

    override fun executeCommand(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            MessageUtils.sendMsg(ctx.event, "Missing arguments")
            return
        }

        val message = """**${ctx.author.asTag}:**
                    |**Input:** ${ctx.argsRaw}
                    |**Output:** ${ctx.argsRaw.reversed()}
                """.trimMargin()

        MessageUtils.sendMsg(ctx.event, message)
    }

    override fun getName() = "reverse"

    override fun help() = """reverses a string
        |Usage: `${Settings.PREFIX}$name <text>`
    """.trimMargin()
}
