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

import Java.lang.VRCubeException;
import ml.duncte123.skybot.commands.animals.*;
import ml.duncte123.skybot.commands.essentials.WolframAlphaCommand;
import ml.duncte123.skybot.commands.essentials.eval.EvalCommand;
import ml.duncte123.skybot.commands.guild.GuildInfoCommand;
import ml.duncte123.skybot.commands.guild.mod.*;
import ml.duncte123.skybot.commands.guild.owner.SettingsCommand;
import ml.duncte123.skybot.commands.uncategorized.BotinfoCommand;
import ml.duncte123.skybot.commands.uncategorized.HelpCommand;
import ml.duncte123.skybot.commands.uncategorized.UserinfoCommand;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class CommandManager {
    
    /**
     * This stores all our commands
     */
    private final Set<Command> commands = ConcurrentHashMap.newKeySet();
    
    /**
     * This makes sure that all the commands are added
     */
    public CommandManager() {
        // default commands
        this.addCommand(new HelpCommand());
        this.addCommand(new UserinfoCommand());
        this.addCommand(new BotinfoCommand());


        //animal commands
        this.addCommand(new LlamaCommand());
        this.addCommand(new CatCommand());
        this.addCommand(new KittyCommand());
        this.addCommand(new DogCommand());
        this.addCommand(new AlpacaCommand());
        this.addCommand(new SealCommand());

        //essentials commands
        this.addCommand(new EvalCommand());

        if (AirUtils.alphaEngine != null)
            this.addCommand(new WolframAlphaCommand());

        //guild commands
        this.addCommand(new GuildInfoCommand());

        //mod commands
        this.addCommand(new BanCommand());
        this.addCommand(new HackbanCommand());
        this.addCommand(new SoftbanCommand());
        this.addCommand(new UnbanCommand());
        this.addCommand(new KickCommand());
        this.addCommand(new CleanupCommand());
        this.addCommand(new AnnounceCommand());

        //Guild owner commands
        this.addCommand(new SettingsCommand());
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
        if (this.commands.parallelStream().anyMatch((cmd) -> cmd.getName().equalsIgnoreCase(command.getName()))) {
            @SinceSkybot(version = "3.52.1")
            List<String> aliases = Arrays.asList(this.commands.parallelStream().filter((cmd) -> cmd.getName().equalsIgnoreCase(command.getName())).findFirst().get().getAliases());
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
     * @param rw the raw message
     * @param event the event for the message
     */
    public void runCommand(String rw, GuildMessageReceivedEvent event) {
        final String[] split = rw.replaceFirst(Pattern.quote(Settings.prefix), "").split("\\s+");
        final String invoke = split[0].toLowerCase();
        final String[] args = Arrays.copyOfRange(split, 1, split.length);

        for (Command c : this.getCommands()) {
            if (invoke.equalsIgnoreCase(c.getName())) {
                c.executeCommand(invoke, args, event);
                return;
            } else {
                for (final String alias : c.getAliases()) {
                    if (invoke.equalsIgnoreCase(alias)) {
                        c.executeCommand(invoke, args, event);
                        return;
                    }
                }
            }
        }
    }
    
}
