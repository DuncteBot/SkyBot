/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import java.io.File

class GeneratePhpCommandList(private val commandManager: CommandManager) {

    fun genPhp() {

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

fun Class<*>.isKotlinClass(): Boolean {
    return this.declaredAnnotations.any {
        it.annotationClass.qualifiedName == "kotlin.Metadata"
    }
}
