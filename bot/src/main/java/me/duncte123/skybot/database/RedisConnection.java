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

package me.duncte123.skybot.database;

import me.duncte123.skybot.objects.discord.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisConnection implements RedisDB {
    /* seconds * minutes * hours * days */
    private static final long TWO_WEEKS_IN_SECONDS = 60L * 60L * 24L * 14L;
    private static final long ONE_MONTH_IN_SECONDS = 60L * 60L * 24L * 31L;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull
    private JedisPool pool;
    private boolean canConnect = true;

    public RedisConnection() {
        connect();
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    private void connect() {
        if (!canConnect) {
            throw new RuntimeException("Shutdown method was called, new connection not allowed");
        }

        String host = System.getenv("REDIS_HOST");
        int port = Protocol.DEFAULT_PORT;

        if (host == null) {
            host = "localhost";
        }

        if (host.contains(":")) {
            final String[] split = host.split(":");
            host = split[0];
            port = Integer.parseInt(split[1]);
        }

        // TODO: can probably just use a URI here
        this.pool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    private Jedis getResource() {
        if (this.pool.isClosed()) {
            connect();
        }

        return this.pool.getResource();
    }

    @Override
    public void storeMessage(@NotNull MessageData data, boolean isPatron) {
        try (Jedis jedis = this.getResource()) {
            // Long hset(String key, Map<String, String> hash);
            jedis.hset(
                data.getMessageIdString(),
                data.toMap()
            );
            // normal 2 weeks, patreon 1 month
            final long seconds = isPatron ? ONE_MONTH_IN_SECONDS : TWO_WEEKS_IN_SECONDS;
            jedis.expire(data.getMessageIdString(), seconds);
        }
    }

    @Override
    @Nullable
    public MessageData getAndUpdateMessage(@NotNull String messageId, @NotNull MessageData updateData, boolean isPatron) {
        try (Jedis jedis = this.getResource()) {
            final Map<String, String> response = jedis.hgetAll(messageId);

            // update the data after getting it
            jedis.hset(messageId, updateData.toMap());
            // update timeout
            jedis.expire(messageId, isPatron ? ONE_MONTH_IN_SECONDS : TWO_WEEKS_IN_SECONDS);

            if (response.isEmpty()) {
                return null;
            }

            return MessageData.from(response);
        }
    }

    @Override
    @Nullable
    public MessageData getAndDeleteMessage(@NotNull String messageId) {
        try (Jedis jedis = this.getResource()) {
            final Map<String, String> response = jedis.hgetAll(messageId);

            if (response.isEmpty()) {
                return null;
            }

            jedis.del(messageId);

            return MessageData.from(response);
        }
    }

    @Override
    @NotNull
    public List<MessageData> getAndDeleteMessages(@NotNull List<String> messageIds) {
        try (Jedis jedis = this.getResource()) {
            final List<MessageData> response = new ArrayList<>();

            for (final String messageId : messageIds) {
                final Map<String, String> data = jedis.hgetAll(messageId);

                if (data.isEmpty()) {
                    continue;
                }

                response.add(MessageData.from(data));
            }

            // delete all messages from the database in one go
            final String[] idArray = messageIds.toArray(String[]::new);

            jedis.del(idArray);

            return response;
        }
    }

    @Override
    public void deleteMessage(@NotNull String messageId) {
        try (Jedis jedis = this.getResource()) {
            jedis.del(messageId);
        }
    }

    @Override
    public void deleteMessages(@NotNull List<String> messageIds) {
        try (Jedis jedis = this.getResource()) {
            final String[] idArray = messageIds.toArray(String[]::new);

            jedis.del(idArray);
        }
    }

    @Override
    public void shutdown() {
        this.canConnect = false;
        this.pool.close();
    }
}
