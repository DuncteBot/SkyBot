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

package ml.duncte123.skybot.web.controllers.api

import com.fasterxml.jackson.databind.ObjectMapper
import ml.duncte123.skybot.CommandManager
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.ICommand

object CommandTransformers {

    fun toJson(commandManager: CommandManager, mapper: ObjectMapper): Any {
        val commands = commandManager.commands
        val output = mapper.createArrayNode()

        for (command in commands) {
            val obj = output.addObject()

            obj.put("name", command.name)
                .put("help", parseHelp(command))

            val aliases = obj.putArray("aliases")

            command.aliases.forEach { aliases.add(it) }
        }

        return output
    }

    fun toPHP(commandManager: CommandManager): Any {
        return buildString {
            appendln("<?php")
            appendln("\$a = [")

            for (command in commandManager.commands) {
                val cls = command.javaClass
                val clsPath = cls.name.replace("\\.".toRegex(), "/")
                val clsName = cls.simpleName

                appendln("\t[")
                appendln("\t\t'name' => '$clsName',")
                appendln("\t\t'path' => '$clsPath',")
                appendln("\t\t'type' => '${if (cls.isKotlinClass()) "kotlin" else "java"}',")
                appendln("\t],")
            }

            appendln("];")
        }
    }

    fun toJekyll(commandManager: CommandManager): Any {
        val names = commandManager.commands
            .filter { it.category != CommandCategory.UNLISTED }
            .map(ICommand::getName)
            .sorted()
            .toList()

        return buildString {
            appendln("---")
            appendln("layout: default")
            appendln("commands:")

            for (name in names) {
                val command = commandManager.getCommand(name)
                val help = parseHelp(command).replace("\"", "\\\"")

                appendln("  - name: $name")
                appendln("    description: \"$help\"")
            }

            appendln("---\n\n{{ content }}")
        }
    }

    private fun parseHelp(cmd: ICommand): String {
        var s = cmd.help()
            .replace("&".toRegex(), "&amp;")
            .replace("<".toRegex(), "&lt;")
            .replace(">".toRegex(), "&gt;")
            .replace("\\n".toRegex(), "<br />")
            .replace("\\`\\`\\`(.*)\\`\\`\\`".toRegex(), "<pre class=\"code-block\"><code>$1</code></pre>")
            .replace("\\`([^\\`]+)\\`".toRegex(), "<code>$1</code>")
            .replace("\\*\\*(.*)\\*\\*".toRegex(), "<strong>$1</strong>")

        if (cmd.aliases.isNotEmpty() && cmd.shouldDisplayAliasesInHelp()) {
            s += "<br />Aliases: " + Settings.PREFIX + cmd.aliases.joinToString(", " + Settings.PREFIX)
        }

        return s
    }

    private fun Class<*>.isKotlinClass(): Boolean {
        return this.declaredAnnotations.any {
            it.annotationClass.qualifiedName == "kotlin.Metadata"
        }
    }
}
