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

package ml.duncte123.skybot;

import Java.lang.VRCubeException;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandManager {
    
    /**
     * This stores all our commands
     */
    private final Set<Command> commands = ConcurrentHashMap.newKeySet();
    
    /**
     * This makes sure that all the commands are added
     */
    public CommandManager() {
        //Get reflections for this project
        Reflections reflections = new Reflections("ml.duncte123.skybot");
        //Only check for things that are commands
        Set<Class<? extends Command>> cmds = reflections.getSubTypesOf(Command.class);
        //Loop over them
        for (Class<? extends Command> cmd : cmds) {
            try {
                //Add the command
                this.addCommand(cmd.newInstance());
            }
            catch (Exception ignored) {
            }
        }
    }
    
    /**
     * This is method to get the commands on request
     *
     * @return A list of all the commands
     */
    public Set<Command> getCommands() {
        return commands;
    }
    
    /**
     * This tries to get a command with the provided name/alias
     *
     * @param name the name of the command
     * @return a possible null command for the name
     */
    public Command getCommand(String name) {
        Optional<Command> cmd = commands.stream().filter(c -> c.getName().equals(name)).findFirst();

        if (cmd.isPresent()) {
            return cmd.get();
        }

        cmd = commands.stream().filter(c -> Arrays.asList(c.getAliases()).contains(name)).findFirst();

        return cmd.orElse(null);
    }

    public List<Command> getCommands(CommandCategory category) {
        return commands.stream().filter(c -> c.getCategory().equals(category)).collect(Collectors.toList());
    }
    
    /**
     * This removes a command from the commands
     *
     * @param command the command to remove
     * @return {@code true} on success
     */
    public boolean removeCommand(String command) {
        return commands.remove(getCommand(command));
    }
    
    /**
     * This handles adding the command
     *
     * @param command The command to add
     * @return true if the command is added
     */
    public boolean addCommand(Command command) {
        if (command.getName().contains(" ")) {
            throw new VRCubeException("Name can't have spaces!");
        }

        //ParallelStream for less execution time
        if (this.commands.stream().anyMatch((cmd) -> cmd.getName().equalsIgnoreCase(command.getName()))) {
            @SinceSkybot(version = "3.52.1")
            List<String> aliases = Arrays.asList(this.commands.stream().filter((cmd) -> cmd.getName().equalsIgnoreCase(command.getName())).findFirst().get().getAliases());
            for (String alias : command.getAliases()) {
                if (aliases.contains(alias)) {
                    return false;
                }
            }
            return false;
        }
        this.commands.add(command);
        
        return true;
    }
    
    /**
     * This will run the command when we need them
     *
     * @param event the event for the message
     */
    public void runCommand(GuildMessageReceivedEvent event) {
        final String[] split = event.getMessage().getContentRaw().replaceFirst(
                Pattern.quote(Settings.prefix) + "|" + Settings.otherPrefix + "|" +
                        Pattern.quote(GuildSettingsUtils.getGuild(event.getGuild()).getCustomPrefix()), "").split("\\s+");
        final String invoke = split[0].toLowerCase();
        final String[] args = Arrays.copyOfRange(split, 1, split.length);

        Command cmd = getCommand(invoke);

        if(cmd != null)
            cmd.executeCommand(invoke, args, event);
    }
    
}
