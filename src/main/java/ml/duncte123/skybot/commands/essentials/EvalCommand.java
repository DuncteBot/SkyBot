package ml.duncte123.skybot.commands.essentials;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class EvalCommand extends Command {

    private ScriptEngine engine;

    /**
     * This initialises the engine
     */
    public EvalCommand() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval("var imports = new JavaImporter(" +
                    "java.io," +
                    "java.lang," +
                    "java.util," +
                    "Packages.net.dv8tion.jda.core," +
                    "Packages.net.dv8tion.jda.core.entities," +
                    "Packages.net.dv8tion.jda.core.entities.impl," +
                    "Packages.net.dv8tion.jda.core.managers," +
                    "Packages.net.dv8tion.jda.core.managers.impl," +
                    "Packages.net.dv8tion.jda.core.utils," +
                    "Packages.ml.duncte123.skybot.utils);");
            engine.put("commands", SkyBot.commands);

        }
        catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        return event.getAuthor().getId().equals(Config.ownerId);
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {

        try {
            engine.put("message", event.getMessage());
            engine.put("channel", event.getChannel());
            engine.put("guild", event.getGuild());
            engine.put("member", event.getMember());
            engine.put("jda", event.getJDA());
            engine.put("event", event);

            engine.eval(
            "function sendMsg(msg) {" +
                "channel.sendMessage(msg).queue();" +
            "}");
            engine.put("args", args);
            Object out = engine.eval("(function() {" +
                            "with (imports) {" +
                                event.getMessage().getRawContent().substring(event.getMessage().getRawContent().split(" ")[0].length()).replaceAll("getToken", "getSelfUser") +
                            "}" +
                        "})();");
           sendMsg(event, out == null ? "Executed without error." : "Result: " + out.toString().replaceAll(event.getJDA().getToken(), "Not Today"));
        }
        catch (Exception e) {
            event.getChannel().sendMessage("Error: " + e.getMessage()).queue();
            e.printStackTrace();
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "A simple eval command (Inspired off of yuis one)";
    }
}
