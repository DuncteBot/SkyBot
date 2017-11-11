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

package ml.duncte123.skybot;

import ml.duncte123.skybot.commands.animals.*;
import ml.duncte123.skybot.commands.essentials.WolframAlphaCommand;
import ml.duncte123.skybot.commands.essentials.eval.EvalCommand;
import ml.duncte123.skybot.commands.fun.*;
import ml.duncte123.skybot.commands.guild.GuildInfoCommand;
import ml.duncte123.skybot.commands.guild.mod.*;
import ml.duncte123.skybot.commands.guild.owner.SettingsCommand;
import ml.duncte123.skybot.commands.music.*;
import ml.duncte123.skybot.commands.uncategorized.*;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.parsers.CommandParser;
import ml.duncte123.skybot.utils.AirUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        this.addCommand(new OneLinerCommands());
        this.addCommand(new UserinfoCommand());
        this.addCommand(new BotinfoCommand());
        this.addCommand(new OneLinerCommandsJava());

        //fun commands
        this.addCommand(new DialogCommand());
        this.addCommand(new KpopCommand());
        this.addCommand(new BlobCommand());
        this.addCommand(new TextToBrickCommand());
        this.addCommand(new JokeCommand());
        this.addCommand(new CoinCommand());
        this.addCommand(new FlipCommand());
        this.addCommand(new TagCommand());

        //animal commands
        this.addCommand(new LlamaCommand());
        this.addCommand(new CatCommand());
        this.addCommand(new KittyCommand());
        this.addCommand(new DogCommand());
        this.addCommand(new AlpacaCommand());
        this.addCommand(new SealCommand());
        this.addCommand(new BirbCommand());

        //essentials commands
        this.addCommand(new EvalCommand());
        
        if(AirUtils.alphaEngine != null)
        	this.addCommand(new WolframAlphaCommand());
        
        //music commands
        this.addCommand(new JoinCommand());
        this.addCommand(new LeaveCommand());
        this.addCommand(new PlayCommand());
        this.addCommand(new StopCommand());
        this.addCommand(new PPlayCommand());
        this.addCommand(new SkipCommand());
        this.addCommand(new PauseCommand());
        this.addCommand(new ListCommand());
        this.addCommand(new NowPlayingCommand());
        this.addCommand(new ShuffleCommand());
        this.addCommand(new RepeatCommand());
        this.addCommand(new PlayRawCommand());

        //guild commands
        this.addCommand(new GuildInfoCommand());

        //mod commands
        this.addCommand(new BanCommand());
        this.addCommand(new HackbanCommand());
        this.addCommand(new SoftbanCommand());
        this.addCommand(new UnbanCommand());
        this.addCommand(new KickCommand());
        this.addCommand(new CleenupCommand());
        this.addCommand(new AnnounceCommand());

        //Guild owner commands
        this.addCommand(new SettingsCommand());

    }

    /**
     * This is method to get the commands on request
     * @return A list of all the commands
     */
    public Set<Command> getCommands() { return commands; }

    /**
     * This tries to get a command with the provided name/alias
     * @param name the name of the command
     * @return a possible null command for the name
     */
    public Command getCommand(String name) {
        Optional<Command> cmd = commands.stream().filter(c->c.getName().equals(name)).findFirst();

        if(cmd.isPresent()) {
            return cmd.get();
        }

        cmd = commands.stream().filter(c-> Arrays.asList(c.getAliases()).contains(name) ).findFirst();

        if(cmd.isPresent()) {
            return cmd.get();
        }

        return null;
    }

    public boolean removeCommand(String command) {
        return commands.remove(getCommand(command));
    }

    /**
     * This handles adding the command
     * @param command The command to add
     * @return true if the command is added
     */
    public boolean addCommand(Command command) {
        if (command.getName().contains(" ")) {
            throw new IllegalArgumentException("Name can't have spaces!");
        }
        
        if (this.commands.stream().map(Command::getName).anyMatch(c -> command.getName().equalsIgnoreCase(c))) {
            return false;
        }
        this.commands.add(command);
        
        return true;
    }

    /**
     * This will run the command when we need them
     * @param parser The command parser used to parse the commands
     */
    public void runCommand(CommandParser.CommandContainer parser) {
        for (Command c : this.getCommands()) {
            if (parser.invoke.equalsIgnoreCase(c.getName())) {
                c.executeCommand(parser.invoke, parser.args, parser.event);
                return;
            } else {
                for (final String alias : c.getAliases()) {
                    if (parser.invoke.equalsIgnoreCase(alias)) {
                        c.executeCommand(parser.invoke, parser.args, parser.event);
                        return;
                    }
                }
            }
        }
    }

}
