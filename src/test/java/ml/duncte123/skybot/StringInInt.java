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

package ml.duncte123.skybot;import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class StringInInt {
    private static Integer integer;

    public static void main(String[] args) throws Throwable {
        Constructor<Unsafe> c = Unsafe.class.getDeclaredConstructor();
        c.setAccessible(true);
        Unsafe u = c.newInstance();

        Field field = StringInInt.class.getDeclaredField("integer");
        Object b = u.staticFieldBase(field);
        long o = u.staticFieldOffset(field);

        u.putObject(b, o, "this is a string");

        //String s = (String)(Object)integer;
        System.out.println(integer);
    }
}