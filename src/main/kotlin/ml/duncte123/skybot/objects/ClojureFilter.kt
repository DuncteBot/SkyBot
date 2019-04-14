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

package ml.duncte123.skybot.objects

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot
import java.util.regex.Pattern

@Author(nickname = "Sanduhr32", author = "Maurice R S")
@SinceSkybot(version = "3.87.1")
class ClojureFilter {
    private val pattern = Pattern.compile("(\\{)(\\(*\\w*\\)*\\s*)?->([\\w\\s()\\[\\]=+\\-*$\"\'\\\\]*)\\}", Pattern.COMMENTS)

    fun filterClojures(script: String): String {
        var matcher = pattern.matcher(script)
        var secondScript = script
        var tempScript: String
        var subscript: String
        var newStart: Int

        while (matcher.find()) {
            tempScript = matcher.group(3)
            newStart = tempScript.indexOf("->") + 2
            subscript = tempScript.substring(newStart).trim()
            tempScript = subscript
            subscript = "\${protectedShell.evaluate(\"$subscript\")}"
            matcher = pattern.matcher(tempScript)
            secondScript = script.replace(tempScript, subscript)
        }

        return secondScript
    }
}

//fun main() {
//    val str = "t = 24; {(xd)-> t = gandalf(t); { -> gandalf(t); }} \${t}"
//    println(ClojureFilter().filterClojures(str))
//}
