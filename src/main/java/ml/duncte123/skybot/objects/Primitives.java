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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Primitives {

    private static final Map<Class<?>, Class<?>> primitivesToWrapper;
    private static final Map<Class<?>, Class<?>> wrapperToPrimitives;

    static {
        Map<Class<?>, Class<?>> p = new HashMap<>(16);
        Map<Class<?>, Class<?>> w = new HashMap<>(16);

        put(p, w, boolean.class, Boolean.class);
        put(p, w, byte.class, Byte.class);
        put(p, w, char.class, Character.class);
        put(p, w, double.class, Double.class);
        put(p, w, float.class, Float.class);
        put(p, w, int.class, Integer.class);
        put(p, w, long.class, Long.class);
        put(p, w, short.class, Short.class);
        put(p, w, void.class, Void.class);

        primitivesToWrapper = Collections.unmodifiableMap(p);
        wrapperToPrimitives = Collections.unmodifiableMap(w);
    }

    public static Class<?> unwrap(Class<?> type) {
        return (wrapperToPrimitives.get(type) == null) ? type : wrapperToPrimitives.get(type);
    }

    public static Class<?> wrap(Class<?> type) {
        return (primitivesToWrapper.get(type) == null) ? type : primitivesToWrapper.get(type);
    }

    private static void put(Map<Class<?>, Class<?>> first, Map<Class<?>, Class<?>> second, Class<?> primitive, Class<?> wrapper) {
        first.put(primitive, wrapper);
        second.put(wrapper, primitive);
    }
}
