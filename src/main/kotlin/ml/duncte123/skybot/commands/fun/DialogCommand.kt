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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import org.apache.commons.text.WordUtils
import java.util.function.BiFunction

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class DialogCommand : Command() {

    init {
        this.category = CommandCategory.FUN
        this.name = "dialog"
        this.helpFunction = BiFunction { _, _ -> "Displays a confirmation dialog" }
        this.usageInstructions = BiFunction { invoke, prefix -> "`$prefix$invoke <text>`" }
    }

    override fun execute(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val lines = WordUtils.wrap(
            ctx.argsDisplay.replace("`", "")
            , 25, null, true).split("\n")

        val string = buildString {
            appendln("```")
            appendln("╔═══════════════════════════╗ ")
            appendln("║ Alert                     ║")
            appendln("╠═══════════════════════════╣")

            lines.stream()
                .map { it.trim() }
                .map { String.format("%-25s", it) }
                .map { "║ $it ║" }
                .forEach { appendln(it) }

            appendln("║  ┌─────────┐  ┌────────┐  ║")
            appendln("║  │   Yes   │  │   No   │  ║")
            appendln("║  └─────────┘  └────────┘  ║")
            appendln("╚═══════════════════════════╝")
            appendln("```")
        }

        MessageUtils.sendMsg(ctx.event, string)
    }
}
