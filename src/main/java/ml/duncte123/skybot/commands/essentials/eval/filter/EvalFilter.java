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

package ml.duncte123.skybot.commands.essentials.eval.filter;

import groovy.lang.Closure;
import groovy.lang.Script;
import ml.duncte123.skybot.exceptions.VRCubeException;
import org.kohsuke.groovy.sandbox.GroovyValueFilter;

import java.util.*;
import java.util.regex.Pattern;

public class EvalFilter extends GroovyValueFilter {

    private static final Set<Class<?>> ALLOWED_TYPES = new HashSet<>();

    /**
     * Filter arrays of 
     * 
     * @author ramidzkh
     */
    public static final Pattern ARRAY_FILTER =
            Pattern.compile(
                    // Case insensitive
                    "(?i)"
                    // Decimals and Octals
                    + "((\\[(\\s*[0-9]+\\s*)\\])"
                    // Binary
                    + "|(\\[(\\s*)(0b)([01_]*)(\\s*)\\])"
                    // Hexadecimal
                    + "|(\\[\\s*(0x)[0-9a-f]+(\\s*)\\]))"),
            MENTION_FILTER = 
                Pattern.compile("<@[0-9]{18}>");

    /**
     * Constructor
     */
    public EvalFilter() {
        ALLOWED_TYPES.addAll(Arrays.asList(ALLOWED_TYPES_LIST));
    }

    /**
     * This filters the script
     * @param o the script to filter
     * @return the script if it passes the filter
     */
    @Override
    public final Object filter(Object o) {
        if (o==null || ALLOWED_TYPES.contains(o.getClass()) )
            return o;
        if(o instanceof Script || o instanceof Closure)
            throw new SecurityException("Scripts/Closures are not allowed, or the variable that you are looking for is not found");
        throw new VRCubeException("Class not allowed: " + o);
    }

    /**
     * This checks if the script contains any loop
     * @param toFilter the script to filter
     * @return true if the script contains a loop
     */
    public boolean filterLoops(String toFilter) {
        return Pattern.compile(
                //for or while loop
                "((while|for)" +
                //Stuff in the brackets
                "(\\s*)\\((\\s*)(([0-9A-Za-z=]*)|([0-9A-Za-z=\\s*;.<>+]*)|(\\s*[;])(\\s*[;]))(\\s*)\\))|" +
                // Other groovy loops
                "(\\.step|\\.times|\\.upto|\\.each)"
                    //match and find
                    ).matcher(toFilter).find();
    }

    /**
     * This checks if the script contains an array
     * @param toFilter the script to filter
     * @return true if the script contains an array
     */
    public boolean filterArrays(String toFilter) {
        //Big thanks to ramidzkh#4814 (https://github.com/ramidzkh) for helping me with this regex
        return ARRAY_FILTER.matcher(toFilter).find();
    }

    /**
     * This contains a list of all the allowed classes
     */
    private static final Class<?>[] ALLOWED_TYPES_LIST = {
            String.class,
            
            Boolean.class,
            boolean.class,
            
            Byte.class,
            byte.class,
            
            Character.class,
            char.class,
            
            Short.class,
            short.class,
            
            Integer.class,
            int.class,
            
            Float.class,
            float.class,
            
            Long.class,
            long.class,
            
            Double.class,
            double.class,
            
            Arrays.class,
            
            List.class,
            ArrayList.class
    };

    public boolean containsMentions(String string) {
        return MENTION_FILTER.matcher(string).find();
    }

}
