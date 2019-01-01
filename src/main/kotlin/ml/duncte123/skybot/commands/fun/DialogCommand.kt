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
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import org.apache.commons.text.WordUtils

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class DialogCommand : Command() {

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            MessageUtils.sendMsg(ctx.event, "Correct usage: `${Settings.PREFIX}$name <words>`")
            return
        }

        val lines = WordUtils.wrap(
            ctx.argsDisplay.replace("`", "")
            , 25, null, true).split("\n")

        val sb = StringBuilder()
            .append("```")
            .append("╔═══════════════════════════╗ \n")
            .append("║ Alert                     ║\n")
            .append("╠═══════════════════════════╣\n")

        lines.stream().map { it.trim() }.map { String.format("%-25s", it) }.map { "║ $it ║\n" }.forEach { sb.append(it) }

        sb.append("║  ┌─────────┐  ┌────────┐  ║\n")
            .append("║  │   Yes   │  │   No   │  ║\n")
            .append("║  └─────────┘  └────────┘  ║\n")
            .append("╚═══════════════════════════╝\n")
            .append("```")
        MessageUtils.sendMsg(ctx.event, sb.toString())
    }

    override fun help() = "Gives you a nice dialog\n" +
        "Usage: `${Settings.PREFIX}$name <text>`"

    override fun getName() = "dialog"
}
