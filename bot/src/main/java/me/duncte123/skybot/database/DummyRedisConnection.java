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

import java.util.List;

public class DummyRedisConnection implements RedisDB {
    @Override
    public void storeMessage(@NotNull MessageData data) {

    }

    @Nullable
    @Override
    public MessageData getAndUpdateMessage(@NotNull String messageId, @NotNull MessageData updateData) {
        return null;
    }

    @Nullable
    @Override
    public MessageData getAndDeleteMessage(@NotNull String messageId) {
        return null;
    }

    @NotNull
    @Override
    public List<MessageData> getAndDeleteMessages(@NotNull List<String> messageIds) {
        return List.of();
    }

    @Override
    public void deleteMessage(@NotNull String messageId) {

    }

    @Override
    public void deleteMessages(@NotNull List<String> messageIds) {

    }

    @Override
    public void shutdown() {

    }
}
