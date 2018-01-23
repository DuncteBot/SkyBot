import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import sun.misc.Unsafe;


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