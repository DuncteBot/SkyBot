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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Make fake interfaces
 *
 * @author ramidzkh
 */
public class FakeInterface<T> {

    protected final Class<T> type;
    protected final Map<Method, InvocationFunction> handlers;

    public FakeInterface(Class<T> type) {
        this(type, new HashMap<>());
    }

    public FakeInterface(Class<T> type, Map<Method, InvocationFunction> handlers) {
        if (type == null) throw new NullPointerException("Type of interface is null");
        if (!type.isInterface())
            throw new IllegalArgumentException("Type " + type + " is not an interface");
        if (type.isAnnotation())
            throw new IllegalArgumentException("Type " + type + " is an annotation");
        this.type = type;
        this.handlers = (handlers != null) ? handlers : new HashMap<>();
    }

    public Class<T> getType() {
        return type;
    }

    public Map<Method, InvocationFunction> getCustomHandlers() {
        return handlers;
    }

    public void populateHandlers(T object) {
        for(Method m : type.getMethods()) {
            handlers.putIfAbsent(m, (i, method, a) -> {
                method.setAccessible(true);
                
                return method.invoke(object, a);
            });
        }
    }

    @SuppressWarnings("unchecked")
    public T create() {
        return (T) Proxy.newProxyInstance(type.getClassLoader(),
                new Class<?>[]{type}, new IH(handlers));
    }

    /**
     * An invocation handler to try and return the proper result type
     *
     * @author ramidzkh
     */
    protected static class IH implements InvocationHandler {
        protected final Map<Method, InvocationFunction> handlers;
        
        protected IH(Map<Method, InvocationFunction> handlers) {
            this.handlers = handlers;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            for (Method m : handlers.keySet())
                if (m.equals(method))
                    return handlers.get(m).handle(proxy, method, args);
            
            if(method.getDeclaringClass() == Object.class)
                return method.invoke(this, args);
            
            // The return type
            Class<?> r = Primitives.unwrap(method.getReturnType());
            
            // Primitives
            if (r.isPrimitive()) {
                return Primitives.wrap(r);
            }
            
            // String
            if (r == String.class | r == CharSequence.class)
                return "";
            
            // Arrays
            if (r.isArray())
                return Array.newInstance(r.getComponentType(), 0);
            
            // List | ArrayList
            if (r == List.class | r == ArrayList.class) //ArrayList extends List so we might use Collection for List, ArrayList, Set and HashSet.
                return new ArrayList<>();
            
            // Set | HashSet
            if (r == Set.class | r == HashSet.class)
                return new HashSet<>();
            
            // Map | HashMap
            if (r == Map.class | r == HashMap.class)
                return new HashMap<>();
            
            // Entry | SimpleEntry
            if(r == Map.Entry.class
                | r == AbstractMap.SimpleEntry.class)
                return new AbstractMap.SimpleEntry<>(null, null);
            
            // Create a fake for that interface
            if (r.isInterface() && !r.isAnnotation())
                return new FakeInterface<>(r).create();
            
            // Constructors
            Stream<Constructor<?>> constructors;
            
            // Try to create an instance
            if ((constructors = Arrays.stream(r.getConstructors())
                                    // public
                                    .filter(Constructor::isAccessible)
                                    // No parameters
                                    .filter(c -> c.getParameterTypes().length == 0)
                                    // No exceptions
                                    .filter(c -> c.getExceptionTypes().length == 0))
                        // If any
                        .count() > 0)
                // For each constructor
                for (Constructor<?> c : constructors.collect(Collectors.toList()))
                    try {
                        // Try to create
                        return c.newInstance();
                    } catch (Error | RuntimeException ignored) {
                        // If we get an Error or RuntimeException, continue
                    }
            
            // Null if we couldn't find
            return null;
        }
    }
}
