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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.objects

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot

@SinceSkybot("3.52.2")
class RadioStream(var name: String, val url: String, val website: String) {
//    private fun hasWebsite() = !website.isNullOrBlank()

//    fun toEmbedString(): String = "[$name]($url) ${if (hasWebsite()) "from [$website]($website)" else ""}"
    fun toEmbedString(): String = "[$name]($url) from [$website]($website)"

    override fun equals(other: Any?): Boolean {
        if (other !is RadioStream) {
            return false
        }

        return this.name == other.name && this.url == other.url && this.website == other.website
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + website.hashCode()
        return result
    }

    override fun toString(): String {
        return "RadioStream(name='$name', url='$url', website=$website)"
    }
}

