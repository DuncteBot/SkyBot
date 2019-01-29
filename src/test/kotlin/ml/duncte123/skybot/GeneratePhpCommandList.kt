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

package ml.duncte123.skybot

import ml.duncte123.skybot.objects.command.Command
import java.io.File
import kotlin.system.exitProcess

class GeneratePhpCommandList {

    companion object {

        @JvmStatic
        fun main(args: Array<out String>) {

            val variables = Variables.getInstance()

            genPhp(variables.commandManager)
            genStaticSite(variables.commandManager)

            exitProcess(0)
        }

        @JvmStatic
        private fun genStaticSite(commandManager: CommandManager) {
            var output = "---\n" +
                "layout: default\n" +
                "commands:\n"

            commandManager.commands.forEach {

                val cmd = it as Command
                val cmdDesc = cmd.helpParsed().replace("\"", "\\\"")

                output += "  - name: ${cmd.name}\n    description: \"$cmdDesc\"\n"
            }

            output += "---\n\n{{ content }}\n"

            val file = File("command_storage.html")
            file.createNewFile()
            file.writeText(output)
        }

        @JvmStatic
        private fun genPhp(commandManager: CommandManager) {

            var s = "<?php\n"
            s += "\$a = [\n"

            commandManager.commands.forEach {
                val cls = it.javaClass
                val clsPath = cls.name.replace("\\.".toRegex(), "/")
                val clsName = cls.simpleName

                s += "\t[\n"

                s += "\t\t'name' => '$clsName',\n"
                s += "\t\t'path' => '$clsPath',\n"
                s += "\t\t'type' => '${ if (cls.isKotlinClass()) "kotlin" else "java" }',\n"

                s += "\t],\n"
            }

            s += "];"

            val file = File("commands.php")
            file.createNewFile()
            file.writeText(s)
        }
    }

}

fun Class<*>.isKotlinClass(): Boolean {
    return this.declaredAnnotations.any {
        it.annotationClass.qualifiedName == "kotlin.Metadata"
    }
}
