package ml.duncte123.skybot.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Primitives {

    public static Class<?> unwrap(Class<?> type) {
        return (wrapperToPrimitives.get(type) == null) ? type : wrapperToPrimitives.get(type);
    }

    public static Class<?> wrap(Class<?> type) {
        return (primitivesToWrapper.get(type) == null) ? type : primitivesToWrapper.get(type);
    }

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

    private static void put(Map<Class<?>, Class<?>> first, Map<Class<?>, Class<?>> second, Class<?> primitive, Class<?> wrapper) {
        first.put(primitive, wrapper);
        second.put(wrapper, primitive);
    }
}
