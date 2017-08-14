package me.duncte123.skybot;

import me.duncte123.skybot.commands.*;
import me.duncte123.skybot.commands.essentials.*;
import me.duncte123.skybot.commands.fun.*;
import me.duncte123.skybot.commands.mod.*;
import me.duncte123.skybot.commands.music.*;
import me.duncte123.skybot.utils.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

import java.util.HashMap;
import java.util.Random;


public class SkyBot {

    private static String logName = Config.defaultName;
    // get a random thing
    public static Random rand = new Random();

    public static JDA jda;
    public static AudioUtils au;

    public static HashMap<String, Command> commands = new HashMap<String, Command>();

    private static CustomLog logger2 = CustomLog.getLog(logName);

    private static String[] messages = {
            "#HYPESQUAD",
            "use " + Config.prefix + "help",
            "V"+Config.version,
            "duncte123.ml",
            "Subscribe???"
    };
    private static int messageIndex = 0;


    public static void main(String[] args){

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
            AirUtils.getWhiteAndBlackList();
        }catch (Exception e) {
            e.printStackTrace();
        }

        //setup commands
        setupCommands();
    }

    public static void updateStatus(){
        messageIndex++;
        if(messageIndex == messages.length){
            messageIndex = 0;
        }
        jda.getPresence().setGame(Game.of(messages[messageIndex]));
    }

    // custom logging
    public static void log(String name, CustomLog.Level lvl, String message){
        logName = name;
        logger2.log(lvl, message);

    }

    public static void log(CustomLog.Level lvl, String message){
        log(Config.defaultName, lvl, message);
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

    private static void setupCommands(){

        // default commands
        commands.put("help", new HelpCommand());
        commands.put("ping", new PingCommand());
        commands.put("coin", new CoinCommand());
        commands.put("cat", new CatCommand());
        commands.put("kitty", new KittyCommand());
        commands.put("dog", new DogCommand());
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

        //essentials commands
        commands.put("whitelist", new WhitelistCommand());
        commands.put("Blacklist", new BlacklistCommand());


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

        //prank commands
        commands.put("ban", new BanCommand());
        commands.put("hackban", new HackbanCommand());
        commands.put("softban", new SoftbanCommand());
        commands.put("unban", new UnbanCommand());
        commands.put("kick", new KickCommand());
    }
}
