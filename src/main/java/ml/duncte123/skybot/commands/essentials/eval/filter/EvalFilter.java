/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ml.duncte123.skybot.commands.essentials.eval.filter;

import org.kohsuke.groovy.sandbox.GroovyValueFilter;

import java.util.*;
import java.util.stream.Collectors;

public class EvalFilter extends GroovyValueFilter {

    private static final Set<Class> ALLOWED_TYPES = new HashSet<>();

    public EvalFilter() {
        ALLOWED_TYPES.addAll(Arrays.stream(ALLOWED_TYPES_LIST).collect(Collectors.toList()));
    }

    @Override
    public final Object filter(Object o) {
        if (o==null || ALLOWED_TYPES.contains(o.getClass()) )
            return o;
        /*if(o instanceof Script || o instanceof Closure)
            return o;*/
        throw new SecurityException("Class not allowed: " + o);
    }

    private static final Class[] ALLOWED_TYPES_LIST = {
            String.class,
            Integer.class,
            Boolean.class,
            Double.class,
            Float.class,
            Short.class,
            Character.class,
            Arrays.class,
            List.class,
            ArrayList.class
    };

}
