package ml.duncte123.skybot.commands.essentials;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;

public class EvalCommand extends Command {

    private ScriptEngine engine;
    private List<String> packageImports;

    /**
     * This initialises the engine
     */
    public EvalCommand() {
        engine = new ScriptEngineManager().getEngineByName("groovy");
        packageImports =  Arrays.asList("java.io",
                "java.lang",
                "java.util",
                "net.dv8tion.jda.core",
                "net.dv8tion.jda.core.entities",
                "net.dv8tion.jda.core.entities.impl",
                "net.dv8tion.jda.core.managers",
                "net.dv8tion.jda.core.managers.impl",
                "net.dv8tion.jda.core.utils",
                "ml.duncte123.skybot.utils");
    }

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if(!event.getAuthor().getId().equals(Settings.ownerId)) {
            sendError(event.getMessage());
            return;
        }

        try {

            Bindings bindings = engine.createBindings();

            bindings.put("commands", AirUtils.commandSetup.getCommands());

            bindings.put("message", event.getMessage());
            bindings.put("channel", event.getChannel());
            bindings.put("guild", event.getGuild());
            bindings.put("member", event.getMember());
            bindings.put("jda", event.getJDA());
            bindings.put("event", event);

            bindings.put("args", args);


            engine.eval(
                "public void sendMsg(String msg) {" +
                        "channel.sendMessage(msg).queue();" +
                    "}", bindings);

            StringBuilder importStringBuilder = new StringBuilder();
            for (final String s : packageImports) {
                importStringBuilder.append("import ").append(s).append(".*;");
            }
            Object out = engine.eval(importStringBuilder.toString() +
                    event.getMessage().getRawContent().substring(event.getMessage().getRawContent().split(" ")[0].length()).replaceAll("getToken", "getSelfUser")
            , bindings);
           sendMsg(event, out == null || String.valueOf(out).isEmpty() ? "Executed without error." : out.toString());
        }
        catch (ScriptException e) {
            System.out.println(e.getMessage());
            event.getChannel().sendMessage("Error: " + e.getMessage()).queue();
            e.printStackTrace();
        }
        catch (Exception e1) {
            event.getChannel().sendMessage("Error: " + e1.getMessage()).queue();
            e1.printStackTrace();
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "A simple eval command";
    }

    @Override
    public String getName() {
        return "eval";
    }
}
