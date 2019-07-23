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

package ml.duncte123.skybot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ShardWatcher {

    private val pings: LongArray = LongArray(SkyBot.shardManager.shardsTotal) { 0 }
    private val logger: Logger = LoggerFactory.getLogger(ShardWatcher::class.java)

    init {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            val shardManager = SkyBot.shardManager

            logger.debug("Checking shards")

            for (shard in shardManager.shardCache) {
                val info = shard.shardInfo
                val ping = shard.ping
                val oldPing = this.pings[info.shardId]

                if (oldPing != ping) {
                    this.pings[info.shardId] = ping
                } else {
                    logger.warn("{} is possibly down", info)
                }
            }

            logger.debug("Checking done")
        }, 10, 10, TimeUnit.MINUTES)
    }
}
