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

package ml.duncte123.skybot.objects

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot

@Author(nickname = "Sanduhr32", author = "Maurice R S")
@SinceSkybot("3.52.2")
open class RadioStream(var name: String, val url: String, val website: String?, val public: Boolean = true) {
    fun hasWebsite() = !website.isNullOrBlank()

    fun toEmbedString(): String = "[$name]($url) ${if (hasWebsite()) "from [$website]($website)" else ""}"
}

class ILoveStream(
        stationName: String,
        val channel: Int,
        val npChannel: Int = channel,
        internal: Boolean = true,
        public: Boolean = true
) : RadioStream(
        name = stationName,
        url = if (internal) "http://stream01.iloveradio.de/iloveradio$channel.mp3" else "http://streams.bigfm.de/${stationName.replace("ilove", "")}ilr-128-mp3",
        website = "http://www.iloveradio.de/streams/",
        public = public
)