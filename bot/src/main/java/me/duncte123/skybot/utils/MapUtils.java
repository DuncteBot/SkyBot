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

import gnu.trove.impl.sync.*;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.*;
import gnu.trove.map.hash.*;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

public class MapUtils {
    private MapUtils() {}

    public static TLongSet newLongSet() {
        return new TSynchronizedLongSet(new TLongHashSet(), new Object());
    }

    public static TLongList newLongList() {
        return new TSynchronizedLongList(new TLongArrayList(), new Object());
    }

    public static TLongIntMap newLongIntMap() {
        return new TSynchronizedLongIntMap(new TLongIntHashMap(), new Object());
    }

    public static TLongLongMap newLongLongMap() {
        return new TSynchronizedLongLongMap(new TLongLongHashMap(), new Object());
    }

    public static <T> TLongObjectMap<T> newLongObjectMap() {
        return new TSynchronizedLongObjectMap<>(new TLongObjectHashMap<>(), new Object());
    }

    public static <T> TObjectLongMap<T> newObjectLongMap() {
        return new TSynchronizedObjectLongMap<>(new TObjectLongHashMap<>(), new Object());
    }

    public static <T> TObjectIntMap<T> newObjectIntMap() {
        return new TSynchronizedObjectIntMap<>(new TObjectIntHashMap<>(), new Object());
    }
}
