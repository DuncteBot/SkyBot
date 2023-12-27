/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:JvmName("TextUtils")

package me.duncte123.skybot.utils

/**
 * Slices text into chunks to make it manageable
 */
fun String.chunkForEmbed(limit: Int = 2000): List<String> {
    val lines = this.split("\n")
    val chunks = mutableListOf<String>()

    var chunk = ""
    lines.forEach { line ->
        if (chunk.length + line.length > limit && chunk.isNotEmpty()) {
            chunks.add(chunk)
            chunk = ""
        }
        if (line.length > limit) {
            line.chunked(limit).forEach { chunks.add(it) }
        } else {
            chunk += "$line\n"
        }
    }

    return chunks
}
