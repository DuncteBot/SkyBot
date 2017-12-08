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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.entities

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.DocumentationNeeded
import ml.duncte123.skybot.SinceSkybot

@Author(nickname = "Sanduhr32", author = "Maurice R S")
@SinceSkybot("3.52.2")
@DocumentationNeeded
class RadioStream(public val name: String, public val url: String, public val website: String?, val public: Boolean = true) {
    fun hasWebsite() = !website.isNullOrBlank()

    override fun toString(): String = "[$name]($url) ${if (hasWebsite()) "from [$website]($website)" else ""}"
}