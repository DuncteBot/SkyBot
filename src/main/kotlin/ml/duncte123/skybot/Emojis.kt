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

package ml.duncte123.skybot

enum class Emojis private constructor(private val unicode: String) {
    KEYCAP_ONE("1\u20E3"),
    // other key caps
    KEYCAP_TEN("\uD83D\uDD1F"),
    MAGNIFICATION_GLASS_RIGHT("\uD83D\uDD0E"),
    RED_CROSS_MARK("\u274C"),
    REPEAT_ONE("\uD83D\uDD02");

    override fun toString(): String = this.unicode

    fun getUnicode(): String = this.unicode
}