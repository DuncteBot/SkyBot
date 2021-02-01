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

package ml.duncte123.skybot.utils

import gnu.trove.list.TLongList
import gnu.trove.list.array.TLongArrayList
import gnu.trove.map.hash.TLongObjectHashMap

class SpamCache : TLongObjectHashMap<TLongList>() {

    @Throws(IllegalArgumentException::class)
    fun update(longs: LongArray, updateMode: Int = 0): SpamCache {
        when {
            updateMode == -1 && longs.size == 1 -> {
                this.remove(longs[0])
            }
            longs.size == 2 -> {
                val msgIds: TLongList =
                    if (!this.containsKey(longs[0])) {
                        TLongArrayList()
                    } else {
                        this[longs[0]] as TLongArrayList
                    }

                if (updateMode == 0) {
                    msgIds.add(longs[1])
                    this.put(longs[0], msgIds)
                } else if (updateMode == 1) {
                    msgIds.remove(longs[1])
                    this.put(longs[0], msgIds)
                }
            }
            else -> {
                throw IllegalArgumentException("Arguments don't match.")
            }
        }
        return this
    }
}
