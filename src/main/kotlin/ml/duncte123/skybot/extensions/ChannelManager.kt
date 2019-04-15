/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import ml.duncte123.skybot.Author
import net.dv8tion.jda.core.managers.ChannelManager
import net.dv8tion.jda.core.utils.Checks
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


@Author(nickname = "duncte123", author = "Duncan Sterken")
fun ChannelManager.setSlowMode(slowmode: Int): ChannelManager {
    setSlowmode(3)
    Checks.check(slowmode in 0..21600, "Slowmode per user must be between 0 and 21600 (seconds)!")

    val f = ChannelManager::class.memberProperties.find { it.name == "slowmode" }

    f?.let {
        it.isAccessible = true
        it.javaField?.isAccessible = true
        it.javaField?.set(this, slowmode)
    }

    return this
}
