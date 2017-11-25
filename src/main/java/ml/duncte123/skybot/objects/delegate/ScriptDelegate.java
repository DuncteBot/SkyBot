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
 */

package ml.duncte123.skybot.objects.delegate;

import Java.lang.VRCubeException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.io.IOException;

public class ScriptDelegate extends Script {
    private int counter;

    public ScriptDelegate(Script s) {
        super(s.getBinding());
    }

    @Override
    public Object run() {
        return "I'm a bot, I can't run.";
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
        throw new VRCubeException("Erm, no?");
    }

    @Override
    public Object evaluate(File file) throws CompilationFailedException, IOException {
        throw new VRCubeException("Erm, no?");
    }

    @Override
    public void run(File file, String[] arguments) throws CompilationFailedException, IOException {
        throw new VRCubeException("Erm, no?");
    }

    public Object dump() {
        return "\uD83D\uDEAE";
    }

    public Object find() {
        return "You will never find me.";
    }

    private synchronized String generateScriptName() {
        return "Script" + (++counter);
    }

    @Override
    public String toString() {
        return generateScriptName();
    }
}
