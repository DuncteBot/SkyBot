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

package ml.duncte123.skybot

import Java.lang.VRCubeException

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class TableFlipException() : VRCubeException() {

    override lateinit var message: String

    private fun getMeme(): String = "(╯°□°）╯︵ ┻━┻"

    constructor(message: String) : this() {
        this.message = message
    }

    override fun getLocalizedMessage(): String = "${getMeme()} $message"
}