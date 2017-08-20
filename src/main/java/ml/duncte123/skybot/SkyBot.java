package ml.duncte123.skybot;

import ml.duncte123.skybot.commands.*;
import ml.duncte123.skybot.commands.animals.*;
import ml.duncte123.skybot.commands.essentials.BlacklistCommand;
import ml.duncte123.skybot.commands.essentials.EvalCommand;
import ml.duncte123.skybot.commands.essentials.WhitelistCommand;
import ml.duncte123.skybot.commands.fun.*;
import ml.duncte123.skybot.commands.mod.*;
import ml.duncte123.skybot.commands.music.*;
import ml.duncte123.skybot.utils.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.HashMap;
import java.util.Random;


public class SkyBot {

    // get a random thing
    public static Random rand = new Random();

    public static JDA jda;
    public static AudioUtils au;

    public static HashMap<String, Command> commands = new HashMap<String, Command>();

    private static String[] messages = {
            "#HYPESQUAD",
            "use " + Config.prefix + "help",
            "V"+Config.version,
            "duncte123.ml",
            "Subscribe???"
    };
    private static int messageIndex = 0;


    public static void main(String[] args) {
        // Load the whit and black list first
        AirUtils.getWhiteAndBlackList();
        // Register our custom logger and turn the default off
        SimpleLog.LEVEL = SimpleLog.Level.OFF;
        SimpleLog.addListener(new CloudLogListener());
        // log in and set up the api
        try{
            jda = new JDABuilder(AccountType.BOT)
                    .setBulkDeleteSplittingEnabled(false)
                    .setAudioEnabled(true)
                    .addEventListener(new BotListener())
                    .setToken(Config.token)
                    .setStatus(OnlineStatus.ONLINE)
                    .setGame(Game.of(messages[messageIndex]))
                    .buildBlocking();
            jda.setAutoReconnect(true);
            au = new AudioUtils();
            //After we have logged in check for people that have added the bot while it was offline.
            AirUtils.checkGuildsOnWhitelist(jda);
        }catch (Exception e) {
            e.printStackTrace();
        }

        //setup commands
        setupCommands();
    }

    /**
     * This is our status loop function
     */
    public static void updateStatus(){
        messageIndex++;
        if(messageIndex == messages.length){
            messageIndex = 0;
        }
        jda.getPresence().setGame(Game.of(messages[messageIndex]));
    }

    // handle the commands
    public static void handleCommand(CommandParser.CommandContainer cmd){
        if(commands.containsKey(cmd.invoke)){
            boolean safe = commands.get(cmd.invoke).called(cmd.args, cmd.event);

            if(!safe){
                commands.get(cmd.invoke).executed(safe, cmd.event);
                return;
            }
            commands.get(cmd.invoke).action(cmd.args, cmd.event);
            commands.get(cmd.invoke).executed(safe, cmd.event);
        }
    }

    /**
     * This will register all our commands
     */
    private static void setupCommands(){

        // default commands
        commands.put("help", new HelpCommand());
        commands.put("ping", new PingCommand());
        commands.put("coin", new CoinCommand());
        commands.put("trigger", new TriggerCommand());
        commands.put("spam", new SpamCommand());
        commands.put("mineh", new MinehCommand());
        commands.put("wam", new WamCommand());
        CleenupCommand cleanupcmd = new CleenupCommand();
        commands.put("cleanup", cleanupcmd);
        commands.put("clear", cleanupcmd);
        commands.put("cookie", new CookieCommand());
        commands.put("potato", new PotatoCommand());
        commands.put("guildinfo", new GuildStatsCommand());
        commands.put("userinfo", new UserinfoCommand());
        commands.put("ttb", new TextToBrickCommand());
        commands.put("botinfo", new BotinfoCommand());
        commands.put("dialog", new DialogCommand());
        commands.put("kpop", new KpopCommand());

        //animal commands
        commands.put("llama", new LlamaCommand());
        commands.put("cat", new CatCommand());
        commands.put("kitty", new KittyCommand());
        commands.put("dog", new DogCommand());
        commands.put("alpaca", new AlpacaCommand());

        //essentials commands
        commands.put("whitelist", new WhitelistCommand());
        commands.put("blacklist", new BlacklistCommand());
        commands.put("eval", new EvalCommand());


        //music commands
        commands.put("join", new JoinCommand());
        commands.put("leave", new LeaveCommand());
        commands.put("play", new PlayCommand());
        commands.put("stop", new StopCommand());
        commands.put("pplay", new PPlayCommand());
        commands.put("skip", new SkipCommand());
        commands.put("pause", new PauseCommand());
        ListCommand listCommand = new ListCommand();
        commands.put("list", listCommand);
        commands.put("queue", listCommand);
        NowPlayingCommand playingCommand = new NowPlayingCommand();
        commands.put("nowplaying", playingCommand);
        commands.put("np", playingCommand);
        commands.put("shuffle", new ShuffleCommand());
        commands.put("repeat", new RepeatCommand());
        commands.put("playrw", new PlayRawCommand());

        //mod commands
        commands.put("ban", new OLD_BanCommand());
        commands.put("hackban", new HackbanCommand());
        commands.put("softban", new SoftbanCommand());
        commands.put("unban", new UnbanCommand());
        commands.put("kick", new KickCommand());
    }
}
