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

package ml.duncte123.skybot.command;

import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.commands.essentials.eval.EvalCommand;
import ml.duncte123.skybot.commands.uncategorized.HelpCommand;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SkybotCommandTest {

    private CommandManager manager = new CommandManager();

    @Test
    public void testCommandGetterForName() {
        String key = "help";
        HelpCommand value = new HelpCommand();

        assertEquals("Retrieved command does not match stored command for same key",
                value, manager.getCommand(key));
    }

    @Test
    public void testCommandGetterForAlias() {
        String key = "evaluate";
        EvalCommand value = new EvalCommand();

        assertEquals("Retrieved command does not match stored command for same key",
                value, manager.getCommand(key));
    }

    @Test
    public void testCommandGetterForNullCommand() {
        String key = "This_SHould_Allways_return_null_for_a_command" + new Random().nextInt();

        assertNull(String.format("Command getter should return null for this name (%s)", key), manager.getCommand(key));
    }
}
