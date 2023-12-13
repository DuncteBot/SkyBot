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

package ml.duncte123.skybot.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class ThreadUtils {

    public static <T> Future<T> runOnVirtual(Supplier<T> action) {
        return runOnVirtual(null, action);
    }

    public static <T> Future<T> runOnVirtual(String name, Supplier<T> action) {
        final var future = new CompletableFuture<T>();
        final var builder = Thread.ofVirtual();

        if (name != null) {
            builder.name(name);
        }

        builder.start(() -> {
            try {
                future.complete(action.get());
            }
            catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public static Future<Void> runOnVirtual(Runnable action) {
        return runOnVirtual(null, action);
    }

    public static Future<Void> runOnVirtual(String name, Runnable action) {
        final var future = new CompletableFuture<Void>();
        final var builder = Thread.ofVirtual();

        if (name != null) {
            builder.name(name);
        }

        builder.start(() -> {
            try {
                action.run();
                future.complete(null);
            }
            catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}

