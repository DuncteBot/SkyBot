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

package me.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.skybot.objects.command.Command
import me.duncte123.skybot.objects.command.CommandContext

class ReverseCommand : Command() {
    init {
        this.name = "reverse"
        this.help = "Reverses a string"
        this.usage = "<text>"
    }

    override fun execute(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val message = """**${ctx.author.asTag}:**
                    |**Input:** ${ctx.argsRaw}
                    |**Output:** ${ctx.argsRaw.reversed()}
        """.trimMargin()

        MessageUtils.sendMsg(ctx, message)
    }
}
