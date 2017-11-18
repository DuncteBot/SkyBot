/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
 *
 */

package ml.duncte123.skybot.objects;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.internal.Primitives;

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
        if(type == null) throw new NullPointerException("Type of interface is null");
        if(!type.isInterface())
            throw new IllegalArgumentException("Type " + type + " is not an interface");
        if(type.isAnnotation())
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
                        new Class<?>[] {type}, new IH(handlers));
    }

    /**
     * An invocation handler to try and return the proper result type
     * 
     * @author ramidzkh
     *
     */
    protected static class IH implements InvocationHandler {
        protected final Map<Method, InvocationFunction> handlers;
        
        protected IH (Map<Method, InvocationFunction> handlers) {
            this.handlers = handlers;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            for(Method m : handlers.keySet())
                if(m.equals(method))
                    return handlers.get(m).handle(proxy, method, args);
            
            // The return type
            Class<?> r = Primitives.unwrap(method.getReturnType());
            
            // Primitives
            if(r.isPrimitive()) {
                if(r == boolean.class)
                    return Boolean.FALSE;
                else if(r == byte.class)
                    return new Byte((byte) 0);
                else if(r == char.class)
                    return '\u0000';
                else if(r == short.class)
                    return new Short((short) 0);
                else if(r == int.class)
                    return new Integer(0);
                else if(r == float.class)
                    return new Float(0F);
                else if(r == long.class)
                    return new Long(0L);
                else if(r == double.class)
                    return new Double(0D);
            }
            
            // String
            if(r == String.class
                | r == CharSequence.class)
                return "";
            
            // Arrays
            if(r.isArray())
                return Array.newInstance(r.getComponentType(), 0);
            
            // List | ArrayList
            if(r == List.class
                | r == ArrayList.class)
                return new ArrayList<>();
            
            // Set | HashSet
            if(r == Set.class
                | r == HashSet.class)
                return new HashSet<>();
            
            // Map | HashMap
            if(r == Map.class
                | r == HashMap.class)
                return new HashMap<>();
            
            // Entry | SimpleEntry
            if(r == Map.Entry.class
                | r == AbstractMap.SimpleEntry.class)
                return new AbstractMap.SimpleEntry<>(null, null);
            
            // Create a fake for that interface
            if(r.isInterface() && !r.isAnnotation())
                return new FakeInterface<>(r).create();
            
            // Constructors
            Stream<Constructor<?>> constructors;
            
            // Try to create an instance
            if((constructors = Arrays.stream(r.getConstructors())
                    // public
                    .filter(Constructor::isAccessible)
                    // No parameters
                    .filter(c -> c.getParameterTypes().length == 0)
                    // No exceptions
                    .filter(c -> c.getExceptionTypes().length == 0))
                    // If any
                    .count() > 0)
                // For each constructor
                for(Constructor<?> c : constructors.collect(Collectors.toList()))
                    try {
                        // Try to create
                        return c.newInstance();
                    } catch (Throwable thr) {
                        // If we get an Error or RuntimeException, continue
                    }
            
            // Null if we couldn't find
            return null;
        }
    }
}
