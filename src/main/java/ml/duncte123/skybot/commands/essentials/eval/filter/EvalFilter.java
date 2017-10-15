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
