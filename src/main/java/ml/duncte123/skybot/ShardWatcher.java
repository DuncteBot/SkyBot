/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.ShardInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ShardWatcher {

    private final long[] pings;
    private final Logger logger = LoggerFactory.getLogger(ShardWatcher.class);

    ShardWatcher(SkyBot skyBot) {
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        final int totalShards = skyBot.getShardManager().getShardsTotal();

        this.pings = new long[totalShards];

        service.scheduleAtFixedRate(this::checkShards, 30, 30, TimeUnit.MINUTES);
    }

    private void checkShards() {

        final ShardManager shardManager = SkyBot.getInstance().getShardManager();

        logger.info("Checking shards");

        for (final JDA shard : shardManager.getShardCache()) {
            final ShardInfo info = shard.getShardInfo();
            final long ping = shard.getPing();
            final long oldPing = this.pings[info.getShardId()];

            if (oldPing != ping) {
                this.pings[info.getShardId()] = ping;
            } else {
                logger.error("{} is possibly down", info);
            }
        }

        logger.info("Checking done");
    }
}
