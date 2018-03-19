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

package ml.duncte123.skybot.objects.delegate;

import ml.duncte123.skybot.exceptions.VRCubeException;
import groovy.lang.Binding;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;

public class ScriptDelegate extends Script {

    private static int counter = 0;

    private int count = ++counter;

    public ScriptDelegate(Script s) {
        super(s.getBinding());
    }

    @Override
    public Object run() {
        return "I am a bot, I can't run.";
    }

    @Override
    public void println() {
        throw new VRCubeException("Hey, i like to keep my console clean");
    }

    @Override
    public void print(Object value) {
        throw new VRCubeException("Hey, i like to keep my console clean");
    }

    @Override
    public void println(Object value) {
        throw new VRCubeException("Hey, i like to keep my console clean");
    }

    @Override
    public void printf(String format, Object value) {
        throw new VRCubeException("Hey, i like to keep my console clean");
    }

    @Override
    public void printf(String format, Object[] values) {
        throw new VRCubeException("Hey, i like to keep my console clean");
    }

    @Override
    public Object evaluate(String expression) throws CompilationFailedException {
        throw new VRCubeException("This method is blocked");
    }

    @Override
    public Object evaluate(File file) throws CompilationFailedException {
        throw new VRCubeException("How did you get access to the file class?");
    }

    @Override
    public void run(File file, String[] arguments) throws CompilationFailedException {
        throw new VRCubeException("I am a bot, I can't run");
    }

    @Override
    public Binding getBinding() {
        Binding b = new Binding();
        b.setProperty("meme", "you");
        return b;
    }

    @Override
    public void setProperty(String property, Object newValue) {
    }

    public Object dump() {
        return "\uD83D\uDEAE";
    }

    public Object find() {
        return "You will never find me.";
    }

    public int quick_mafs(int x) {
        int the_thing = x + 2 - 1;
        return the_thing;
    }

    public boolean isEven(int number) {
        return number % 2 == 0;
    }

    @Override
    public String toString() {
        return "Script" + count;
    }
}
