/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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

package ml.duncte123.skybot.utils

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.DocumentationNeeded
import ml.duncte123.skybot.SinceSkybot
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

@SinceSkybot("3.51.5")
@DocumentationNeeded
@Author(nickname = "Sanduhr32", author = "Maurice R S")
class EarthUtils {
    companion object {
        @JvmStatic
        fun throwableToJSONObject(throwable: Throwable): JSONObject {
            return JSONObject().put("className", throwable::class.java.name)
                        .put("message", throwable.message)
                        .put("localiziedMessage", throwable.localizedMessage)
                        .put("cause", throwable.cause?.let { throwableToJSONObject(it) })
                        .put("supressed", throwableArrayToJSONArray(throwable.suppressed))
                        .put("stacktraces", stacktraceArrayToJSONArray(throwable.stackTrace))
        }

        @JvmStatic
        private fun throwableArrayToJSONArray(throwables: Array<Throwable>) =
                JSONArray(throwables.map { throwableToJSONObject(it) })

        @JvmStatic
        private fun stacktraceArrayToJSONArray(stackTraces: Array<StackTraceElement>): JSONArray =
                JSONArray(stackTraces.map { stackTraceToJSONObject(it) })

        @JvmStatic
        private fun stackTraceToJSONObject(stackTraceElement: StackTraceElement) =
                JSONObject().put("className", stackTraceElement.className)
                            .put("methodName", stackTraceElement.methodName)
                            .put("lineNumber", stackTraceElement.lineNumber)
                            .put("isNative", stackTraceElement.isNativeMethod)

        @JvmStatic
        fun write(file: String, content: String) {
            val file = File(file)

            if (!file.exists())
                file.createNewFile()

            FileOutputStream(file).write(content.toByteArray())
        }
    }
}