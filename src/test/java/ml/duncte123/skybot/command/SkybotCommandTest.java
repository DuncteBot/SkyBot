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

package ml.duncte123.skybot.command;

import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.commands.essentials.eval.EvalCommand;
import ml.duncte123.skybot.commands.uncategorized.HelpCommand;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class SkybotCommandTest {

    private final CommandManager manager = new CommandManager();

    @Test
    public void testCommandGetterForName() {
        String key = "help";
        HelpCommand value = new HelpCommand();

        assertEquals("Retrieved command does not match stored command for same key (Name)",
                value, manager.getCommand(key));
    }

    @Test
    public void testCommandGetterForAlias() {
        String key = "evaluate";
        EvalCommand value = new EvalCommand();

        assertEquals("Retrieved command does not match stored command for same key (Alias)",
                value, manager.getCommand(key));
    }

    @Test
    public void testCommandGetterForNullCommand() {
        String key = "This_SHould_Allways_return_null_for_a_command" + new Random().nextInt();

        assertNull(String.format("Command getter should return null for this name (%s)", key), manager.getCommand(key));
    }

    @Test
    public void testCommandRegistering() {
        manager.addCommand(new DummyCommand());

        assertNotNull("The dummy command is not registered", manager.getCommand("dummy"));
        assertTrue("Could not remove the dummy command", manager.removeCommand("dummy"));
    }

    @Test
    public void testCommandRun() {
        DummyCommand cmd = new DummyCommand();
        manager.addCommand(cmd);
        //We are not using the args so they can be null
        manager.runCommand(new FakeGuildMessageReceivedEvent(cmd));

        assertTrue("Command did not run", cmd.hasRun);
    }
}
