package ml.duncte123.skybot.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Primitives {

    static Class<?> unwrap(Class<?> type) {
        return (wrapperToPrimitives.get(type) == null) ? type : wrapperToPrimitives.get(type);
    }

    static Class<?> wrap(Class<?> type) {
        return (primitivesToWrapper.get(type) == null) ? type : primitivesToWrapper.get(type);
    }

    private static final Map<Class<?>, Class<?>> primitivesToWrapper = new HashMap<>(16);
    private static final Map<Class<?>, Class<?>> wrapperToPrimitives = new HashMap<>(16);

    static {
        Map<Class<?>, Class<?>> primitivesToWrapper = new HashMap<>(16);
        Map<Class<?>, Class<?>> wrapperToPrimitives = new HashMap<>(16);

        put(primitivesToWrapper, wrapperToPrimitives, boolean.class, Boolean.class);
        put(primitivesToWrapper, wrapperToPrimitives, byte.class, Byte.class);
        put(primitivesToWrapper, wrapperToPrimitives, char.class, Character.class);
        put(primitivesToWrapper, wrapperToPrimitives, double.class, Double.class);
        put(primitivesToWrapper, wrapperToPrimitives, float.class, Float.class);
        put(primitivesToWrapper, wrapperToPrimitives, int.class, Integer.class);
        put(primitivesToWrapper, wrapperToPrimitives, long.class, Long.class);
        put(primitivesToWrapper, wrapperToPrimitives, short.class, Short.class);
        put(primitivesToWrapper, wrapperToPrimitives, void.class, Void.class);

        primitivesToWrapper = Collections.unmodifiableMap(primitivesToWrapper);
        wrapperToPrimitives = Collections.unmodifiableMap(primitivesToWrapper);
    }

    private static void put(Map<Class<?>, Class<?>> first, Map<Class<?>, Class<?>> second, Class<?> primitive, Class<?> wrapper) {
        first.put(primitive, wrapper);
        second.put(wrapper, primitive);
    }
}
