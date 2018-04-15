/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.objects;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class Primitives {

    private static final BiMap<Class<?>, Class<?>> map;

    static {
        Map<Class<?>, Class<?>> temp = new HashMap<>();
        
        temp.put(boolean.class, Boolean.class);
        temp.put(byte.class, Byte.class);
        temp.put(char.class, Character.class);
        temp.put(double.class, Double.class);
        temp.put(float.class, Float.class);
        temp.put(int.class, Integer.class);
        temp.put(long.class, Long.class);
        temp.put(short.class, Short.class);
        temp.put(void.class, Void.class);

        map = ImmutableBiMap.copyOf(temp);
    }

    public static Class<?> wrap(Class<?> type) {
        if(type == null || !type.isPrimitive()) return null;
        return map.get(type);
    }

    public static Class<?> unwrap(Class<?> type) {
        if(type == null || !type.isPrimitive()) return null;
        return map.inverse().get(type);
    }
}
