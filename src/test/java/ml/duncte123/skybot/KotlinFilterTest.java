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

package ml.duncte123.skybot;

import groovy.lang.GroovyShell;
import ml.duncte123.skybot.commands.essentials.eval.filter.KotlinEvalFilter;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

public class KotlinFilterTest {
    
    public static void main(String[] args) {
        System.out.println(eval("2+2"));
        System.out.println(eval("'memes'"));
        System.out.println(eval("System.out"));
        System.out.println(eval("java.math.BigInteger.valueOf(2)"));
    }
    
    public static Object eval(String s) {
        GroovyShell gs = new GroovyShell(new CompilerConfiguration().addCompilationCustomizers(new SandboxTransformer()));
        
        KotlinEvalFilter filter = new KotlinEvalFilter();
        
        filter.register();
        
        Object o = null;
        
        try {
            o = gs.evaluate(s);
        } catch (Throwable thr) {
            thr.printStackTrace();
        } finally {
            filter.unregister();
        }
        
        return o;
    }
}
