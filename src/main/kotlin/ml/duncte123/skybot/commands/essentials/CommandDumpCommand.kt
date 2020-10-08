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

package ml.duncte123.skybot.commands.essentials

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import ml.duncte123.skybot.CommandManager
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.CommandUtils.isDev

class CommandDumpCommand : Command() {
    init {
        this.category = CommandCategory.UNLISTED
        this.name = "commandsdump"
    }

    override fun execute(ctx: CommandContext) {
        if (!isDev(ctx.author)) {
            return
        }

        val jackson = ctx.variables.jackson
        val jsonList = parseCommandsToJson(ctx.commandManager, jackson)
        val data = jackson.writeValueAsBytes(jsonList)

        ctx.channel.sendFile(data, "commands.json").queue()
    }

    private fun parseCommandsToJson(commandManager: CommandManager, mapper: JsonMapper): ArrayNode {
        val commands = commandManager.getFilteredCommands().sortedBy { it.name }
        val output = mapper.createArrayNode()

        for (command in commands) {
            val obj = output.addObject()

            obj.put("name", command.name)
                .put("help", command.parseHelp())

            val aliases = obj.putArray("aliases")

            command.aliases.forEach { aliases.add(it) }
        }

        return output
    }

    private fun Command.parseHelp(): String {
        val ownHelp = this.getHelp(this.name, Settings.PREFIX).mdToHtml()
        var s = "$ownHelp<br />Usage: ${this.getUsageInstructions(Settings.PREFIX, this.name).mdToHtml()}"

        if (this.aliases.isNotEmpty()) {
            val aliasHelp = getHelp(this.aliases[0], Settings.PREFIX).mdToHtml()

            s += if (aliasHelp == ownHelp) {
                "<br />Aliases: " + Settings.PREFIX + this.aliases.joinToString(", " + Settings.PREFIX)
            } else {
                buildString {
                    this@parseHelp.aliases.forEach {
                        append("<br />${Settings.PREFIX}$it => ${getHelp(it, Settings.PREFIX).mdToHtml()}")
                        append("<br />Usage: ${getUsageInstructions(Settings.PREFIX, it).mdToHtml()}")
                    }
                }
            }
        }

        return s
    }

    private fun String.mdToHtml(): String {
        return this.replace("&".toRegex(), "&amp;")
            .replace("<".toRegex(), "&lt;")
            .replace(">".toRegex(), "&gt;")
            .replace("\\n".toRegex(), "<br />")
            .replace("\\`\\`\\`(.*)\\`\\`\\`".toRegex(), "<pre class=\"code-block\"><code>$1</code></pre>")
            .replace("\\`([^\\`]+)\\`".toRegex(), "<code>$1</code>")
            .replace("\\*\\*(.*)\\*\\*".toRegex(), "<strong>$1</strong>")
    }

    private fun CommandManager.getFilteredCommands(): List<Command> {
        return this.commandsList.filter { it.category != CommandCategory.UNLISTED }.map { it as Command }
    }
}
