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

package ml.duncte123.skybot.objects

import net.jodah.expiringmap.ExpiringMap

/**
 * Custom wrapper for [ExpiringMap] that has a nullable getter for [getIfPresent]
 */
class DBMap<K, V>(private val realMap: ExpiringMap<K, V>) : Map<K, V> by realMap {
    fun getIfPresent(key: K): V? {
        return if (realMap.containsKey(key)) {
            realMap[key]
        } else {
            null
        }
    }

    fun remove(key: K) = realMap.remove(key)

    operator fun set(guildId: K, value: V) {
        realMap[guildId] = value
    }
}
