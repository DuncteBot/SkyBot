/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * An {@link InvocationFunction} is a {@link InvocationHandler} in its basic form
 * 
 * @author ramidzkh
 *
 */
@FunctionalInterface
public interface InvocationFunction {

    /**
     * Handles a function
     * 
     * @param instance The {@link Proxy proxy} used
     * @param method The method invoked
     * @param args Parameters, if any
     * @return The returned object. If the method needs to return a primitive type,
     * its corresponding wrapper type must be returned
     * @throws Throwable so it can be any error java has
     */
    Object handle(Object instance, Method method, Object... args) throws Throwable;
}
