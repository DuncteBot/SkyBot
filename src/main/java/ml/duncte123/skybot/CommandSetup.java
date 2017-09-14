package ml.duncte123.skybot;

import ml.duncte123.skybot.commands.animals.*;
import ml.duncte123.skybot.commands.essentials.EvalCommand;
import ml.duncte123.skybot.commands.fun.*;
import ml.duncte123.skybot.commands.guild.GuildInfoCommand;
import ml.duncte123.skybot.commands.guild.mod.*;
import ml.duncte123.skybot.commands.guild.owner.SettingsCommand;
import ml.duncte123.skybot.commands.music.*;
import ml.duncte123.skybot.commands.uncategorized.*;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.CommandParser;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommandSetup {

    /**
     * This stores all our commands
     */
    private final Set<Command> commands = ConcurrentHashMap.newKeySet();

    /**
     * This makes sure that all the commands are added
     */
    public CommandSetup() {

        // default commands
        this.addCommand(new HelpCommand());
        this.addCommand(new PingCommand());
        this.addCommand(new CoinCommand());
        this.addCommand(new TriggerCommand());
        this.addCommand(new SpamCommand());
        this.addCommand(new MinehCommand());
        this.addCommand(new WamCommand());
        this.addCommand(new CookieCommand());
        this.addCommand(new PotatoCommand());
        this.addCommand(new GuildInfoCommand());
        this.addCommand(new UserinfoCommand());
        this.addCommand(new TextToBrickCommand());
        this.addCommand(new BotinfoCommand());
        this.addCommand(new DialogCommand());
        this.addCommand(new KpopCommand());

        //animal commands
        this.addCommand(new LlamaCommand());
        this.addCommand(new CatCommand());
        this.addCommand(new KittyCommand());
        this.addCommand(new DogCommand());
        this.addCommand(new AlpacaCommand());
        this.addCommand(new SealCommand());

        //essentials commands
        this.addCommand(new EvalCommand());


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
     * This handles adding the command
     * @param command The command to add
     * @return true if the command is added
     */
    public boolean addCommand(Command command) {
        if (!command.getName().contains(" ")) {
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
            if (parser.invoke.toLowerCase().startsWith(c.getName().toLowerCase() + ' ') || parser.invoke.equalsIgnoreCase(c.getName())) {
                c.executeCommand(parser.args, parser.event);
                return;
            } else {
                for (final String alias : c.getAliases()) {
                    if (parser.invoke.toLowerCase().startsWith(alias.toLowerCase() + ' ') || parser.invoke.equalsIgnoreCase(alias)) {
                        c.executeCommand(parser.args, parser.event);
                        return;
                    }
                }
            }
        }
    }

}
