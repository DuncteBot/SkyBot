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

package ml.duncte123.skybot.utils;

import gnu.trove.impl.sync.*;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

public class MapUtils {
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
}
