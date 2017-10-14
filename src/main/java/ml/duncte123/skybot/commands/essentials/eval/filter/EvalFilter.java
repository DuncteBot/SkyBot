package ml.duncte123.skybot.commands.essentials.eval.filter;

import groovy.lang.Closure;
import groovy.lang.Script;
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
        if (o==null || ALLOWED_TYPES.contains(o) )
            return o;
        /*if(o instanceof Script || o instanceof Closure)
            return o;*/
        throw new SecurityException("Class not allowed: " + o.toString());
    }

    private static final Class[] ALLOWED_TYPES_LIST = {
            String.class,
            Integer.class,
            Boolean.class,
            Double.class,
            Float.class,
            Short.class,
            Byte.class,
            Character.class,
            Math.class,
            Arrays.class,
            ArrayList.class,
            List.class
    };


}
