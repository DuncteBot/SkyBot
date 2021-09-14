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
import net.dv8tion.jda.api.entities.Message;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {
    private final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

    public void test(Message message) {
        try (Jedis jedis = this.pool.getResource()) {
            // Long hset(String key, Map<String, String> hash);
            jedis.hset(
                message.getId(),
                MessageData.from(message).toMap()
            );
        }
    }

    public MessageData testGet(String messageId) {
        try (Jedis jedis = this.pool.getResource()) {
            return MessageData.from(
                jedis.hgetAll(messageId)
            );
        }
    }

    public void shutdown() {
        this.pool.close();
    }
}
