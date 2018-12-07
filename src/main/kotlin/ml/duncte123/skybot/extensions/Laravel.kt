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

package ml.duncte123.skybot.extensions

import java.util.Base64.getUrlEncoder as MagicClass
import java.util.Base64.getUrlDecoder as MagicClass2

private const val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789:/.="
private const val target = "rCnZk8Aw9P0Y24sUM6x5tKRl31L7IhJifDdcbueoOqHavByzGpVSXmjNWTgQFE[!$ "

fun String.eloquent(): String {

    val magic = MagicClass().encodeToString(toByteArray())

    val result = CharArray(magic.length)
    for (i in 0 until magic.length) {
        val c = magic[i]
        val index = source.indexOf(c)

        if (index == -1) {
            result[i] = c
            continue
        }

        result[i] = target[index]
    }

    return String(result).trim()

}

fun String.illuminate(): String {

    val thing = this.substring(25)

    val result = CharArray(thing.length)
    for (i in 0 until thing.length) {
        val c = thing[i]
        val index = target.indexOf(c)

        if (index == -1) {
            result[i] = c
            continue
        }

        result[i] = source[index]
    }

    return String(MagicClass2().decode(String(result).trim())).trim()
}

fun String.cdnPrefix(): String {
    return "https://cdn.duncte123.me/${eloquent()}"
}
