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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.unstable.utils

import ml.duncte123.skybot.Author
import net.dv8tion.jda.core.entities.MessageChannel
import org.apache.commons.lang3.StringUtils

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class ComparatingUtils {

    companion object {
        @JvmField
        var exceptionMap: Map<Class<*>, Map<ExceptionType, Array<out StackTraceElement>>> = HashMap()

        @JvmStatic
        inline fun <reified T : Throwable> checkEx(throwable: T): BooleanArray {
            val mapHasKey = exceptionMap.containsKey(throwable::class.java)
            val exactMatch =
                    if (mapHasKey) {
                        exceptionMap[throwable::class.java]!!.entries.filter { it.key.ex compare throwable }.count() > 0
                    } else {
                        false
                    }
            val added: Boolean = if (!mapHasKey && !exactMatch) {
                exceptionMap += throwable::class.java to hashMapOf(ExceptionType(throwable) to throwable.stackTrace)
                true
            } else if (mapHasKey && !exactMatch) {
                var lowerMap = exceptionMap[throwable::class.java]!!
                lowerMap += hashMapOf(ExceptionType(throwable) to throwable.stackTrace)
                exceptionMap += throwable::class.java to lowerMap
                true
            } else {
                exceptionMap[throwable::class.java]!!.keys.first().increase()
                false
            }
            return booleanArrayOf(mapHasKey, exactMatch, added)
        }

        @JvmStatic
        fun execCheck(throwable: Throwable): BooleanArray {
            return checkEx(throwable)
        }

        @JvmStatic
        fun provideData(channel: MessageChannel) {
            val headers = listOf("Exception Class", "Types", "StackTrace length")
            val table: ArrayList<List<String>> = ArrayList()
            exceptionMap.forEach { cls, lowerMap ->
                val row = ArrayList<String>()
                row.add(cls.name)
                row.add(lowerMap.keys.size.toString())
                row.add(lowerMap.values.map { it.size.toDouble() }.average().toString())
                table.add(row)
            }
            channel.sendMessage(makeAsciiTable(headers, table)).queue()
        }

        @JvmStatic
        fun provideExactData(channel: MessageChannel) {
            val headers = listOf("Exception Class", "Types", "Count", "Message", "Trace length")
            val table: ArrayList<List<String>> = ArrayList()
            exceptionMap.forEach { cls, lowerMap ->
                var row = ArrayList<String>()
                lowerMap.forEach { type, trace ->
                    row.add(cls.name); row.add(type.ex::class.java.name); row.add(type.count.toString()); row.add(type.message); row.add(trace.size.toString())
                    table.add(row)
                    row = ArrayList()
                }
            }
            channel.sendMessage(makeAsciiTable(headers, table)).queue()
        }

        private fun makeAsciiTable(headers: List<String>, table: List<List<String>>): String {
            val sb = StringBuilder()
            val padding = 1
            val widths = IntArray(headers.size)
            for (i in widths.indices) {
                widths[i] = 0
            }
            headers.indices
                    .filter { headers[it].length > widths[it] }
                    .forEach { widths[it] = headers[it].length }
            for (row in table) {
                for (i in row.indices) {
                    val cell = row[i]
                    if (cell.length > widths[i]) {
                        widths[i] = cell.length
                    }
                }
            }
            sb.append("```").append("prolog").append("\n")
            val formatLine = StringBuilder("║")
            for (width in widths) {
                formatLine.append(" %-").append(width).append("s ║")
            }
            formatLine.append("\n")
            sb.append(appendSeparatorLine("╔", "╦", "╗", padding, *widths))
            sb.append(String.format(formatLine.toString(), *headers.toTypedArray()))
            sb.append(appendSeparatorLine("╠", "╬", "╣", padding, *widths))
            for (row in table) {
                sb.append(String.format(formatLine.toString(), *row.toTypedArray()))
            }
            sb.append(appendSeparatorLine("╚", "╩", "╝", padding, *widths))
            sb.append("```")
            return sb.toString()
        }

        private fun appendSeparatorLine(left: String, middle: String, right: String, padding: Int, vararg sizes: Int): String {
            var first = true
            val ret = StringBuilder()
            for (size in sizes) {
                if (first) {
                    first = false
                    ret.append(left).append(StringUtils.repeat("═", size + padding * 2))
                } else {
                    ret.append(middle).append(StringUtils.repeat("═", size + padding * 2))
                }
            }
            return ret.append(right).append("\n").toString()
        }
    }



    class ExceptionType(public val ex: Throwable, public val message: String, public val stackTrace: Array<out StackTraceElement>) {
        constructor(ex: Throwable) : this(ex, ex.localizedMessage, ex.stackTrace)
        public var count = 1L

        public fun increase() {
            ++count
        }
    }
}

infix fun Throwable.compare(other: Throwable): Boolean {
    val classesMatch = this::class.java == other::class.java
    val messageMatch = this.localizedMessage == other.localizedMessage
    val stacktraceMatch = other.stackTrace.map { this.stackTrace.contains(it) }.filter { false }.count() < 4
    return classesMatch && messageMatch || messageMatch && stacktraceMatch
}