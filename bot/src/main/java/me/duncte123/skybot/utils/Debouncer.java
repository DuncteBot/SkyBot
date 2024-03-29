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

package me.duncte123.skybot.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Debouncer<T> implements Consumer<T> {

    private final Consumer<T> consumer;
    private final AtomicLong lastCalled = new AtomicLong(0);
    private final long interval;

    public Debouncer(Consumer<T> consumer, long interval) {
        this.consumer = consumer;
        this.interval = interval;
    }

    @Override
    public void accept(T arg) {
        if (lastCalled.get() + interval < System.currentTimeMillis()) {
            lastCalled.set(System.currentTimeMillis());
            consumer.accept(arg);
        }
    }
}
