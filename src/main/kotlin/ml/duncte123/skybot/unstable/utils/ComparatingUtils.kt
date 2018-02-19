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
@file:JvmName("ComparatingUtilsKt")

package ml.duncte123.skybot.unstable.utils

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.utils.MessageUtils
import ml.duncte123.skybot.utils.TextColor
import ml.duncte123.skybot.utils.hastebin
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.TextChannel
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class ComparatingUtils {

    companion object {
        @JvmStatic
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
            val data = booleanArrayOf(mapHasKey, exactMatch, added)
            LoggerFactory.getLogger(ComparatingUtils::class.java)
                    .debug("${TextColor.CYAN}ExceptionData: [HadKey: ${data[0]}, HadMatching: ${data[1]}, Added: ${data[2]}]${TextColor.RESET}")
            return data
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
            if(channel is TextChannel) MessageUtils.sendMsg(channel, makeAsciiTable(headers, table))
            else channel.sendMessage(makeAsciiTable(headers, table)).queue()
        }

        @JvmStatic
        fun provideExactData(channel: MessageChannel) {
            val headers = listOf("Exception Class", "Count", "Message", "Trace length")
            val table: ArrayList<List<String>> = ArrayList()
            exceptionMap.forEach { cls, lowerMap ->
                var row = ArrayList<String>()
                lowerMap.forEach { type, trace ->
                    row.add(cls.name); row.add(type.count.toString()); row.add(type.message); row.add(trace.size.toString())
                    table.add(row)
                    row = ArrayList()
                }
            }
            if(channel is TextChannel) MessageUtils.sendMsg(channel, makeAsciiTable(headers, table))
            else channel.sendMessage(makeAsciiTable(headers, table)).queue()
        }

        fun provideAtomicData(channel: MessageChannel, ex: String) {
            val headers = listOf("Type", "Message", "Count", "Trace")
            val table: ArrayList<List<String>> = ArrayList()
            val data = exceptionMap.entries.first { it.key.name == ex }.value

            val hastedata = data.keys.map { it.ex.printStackTrace("") }.joinToString(separator = "\n\n\n\n================================\n\n\n\n", prefix = "\n\n\n\n")

            val haste = hastebin(hastedata)

            data.keys.forEachIndexed { index, exceptionType ->
                table.add(listOf("$index", exceptionType.message, "${exceptionType.count}", haste))
            }

            if(channel is TextChannel) MessageUtils.sendMsg(channel, makeAsciiTable(headers, table))
            else channel.sendMessage(makeAsciiTable(headers, table)).queue()
        }

        private fun makeAsciiTable(headers: List<String>, table: List<List<String>>): String {
            val sb = StringBuilder()
            val padding = 1
            val widths = IntArray(size = headers.size)
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
            sb.append(appendSeparatorLine(left = "╔", middle = "╦", right = "╗", padding = padding, sizes = *widths))
            sb.append(String.format(format = formatLine.toString(), args = *headers.toTypedArray()))
            sb.append(appendSeparatorLine(left = "╠", middle = "╬", right = "╣", padding = padding, sizes = *widths))
            for (row in table) {
                sb.append(String.format(format = formatLine.toString(), args = *row.toTypedArray()))
            }
            sb.append(appendSeparatorLine(left = "╚", middle = "╩", right = "╝", padding = padding, sizes = *widths))
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

    class ExceptionType(val ex: Throwable, val message: String, val stackTrace: Array<out StackTraceElement>) {
        constructor(ex: Throwable) : this(ex, ex.localizedMessage, ex.stackTrace)

        var count = 1L

        fun increase() {
            ++count
        }
    }
}

infix fun Throwable.compare(other: Throwable): Boolean {
    val classesMatch = this::class.java == other::class.java
    val messageMatch = this.localizedMessage == other.localizedMessage
    val stacktraceMatch = other.stackTrace.map { this.stackTrace.contains(it) }.filter { false }.count() < 4
    return (classesMatch && stacktraceMatch && messageMatch) || (classesMatch && stacktraceMatch || messageMatch && stacktraceMatch)
}

infix fun Throwable.printStackTrace(input: String): String {
    var s = input
    s += "${this::class.java.name}: ${this.localizedMessage}\n"
    this.stackTrace.forEach {
        s += "\t$it\n"
    }
    return s
}