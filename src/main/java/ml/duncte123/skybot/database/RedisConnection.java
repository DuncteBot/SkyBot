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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.database;

import ml.duncte123.skybot.objects.discord.MessageData;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

public class RedisConnection {
    private final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

    public void storeMessage(MessageData data) {
        try (final Jedis jedis = this.pool.getResource()) {
            // Long hset(String key, Map<String, String> hash);
            jedis.hset(
                data.getMessageIdString(),
                data.toMap()
            );
            // normal 1 month, patreom 5 months
            jedis.expire(data.getMessageIdString(), 0L);
        }
    }

    // NOTE: not optimized update and insert operations
    @Nullable
    public MessageData getMessage(String messageId) {
        try (final Jedis jedis = this.pool.getResource()) {
            final Map<String, String> response = jedis.hgetAll(messageId);

            if (response.isEmpty()) {
                return null;
            }

            return MessageData.from(response);
        }
    }

    @Nullable
    public MessageData getAndUpdateMessage(String messageId, MessageData updateData) {
        try (final Jedis jedis = this.pool.getResource()) {
            final Map<String, String> response = jedis.hgetAll(messageId);

            // update the data after getting it
            jedis.hset(messageId, updateData.toMap());
            // TODO: update timeout

            if (response.isEmpty()) {
                return null;
            }

            return MessageData.from(response);
        }
    }

    @Nullable
    public MessageData getAndDeleteMessage(String messageId) {
        try (final Jedis jedis = this.pool.getResource()) {
            final Map<String, String> response = jedis.hgetAll(messageId);

            if (response.isEmpty()) {
                return null;
            }

            jedis.del(messageId);

            return MessageData.from(response);
        }
    }

    public void deleteMessage(String messageId) {
        try (final Jedis jedis = this.pool.getResource()) {
            jedis.del(messageId);
        }
    }

    public void shutdown() {
        this.pool.close();
    }
}
