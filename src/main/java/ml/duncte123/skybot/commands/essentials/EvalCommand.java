package ml.duncte123.skybot.commands.essentials;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class EvalCommand extends Command {

    private ScriptEngine engine;

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

        }
        catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        return event.getAuthor().getId().equals(Config.ownerId);
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {

        try {
            engine.put("event", event);
            engine.put("message", event.getMessage());
            engine.put("channel", event.getChannel());
            engine.put("args", args);
            engine.put("jda", event.getJDA());
            engine.put("commands", SkyBot.commands);

            if (event.isFromType(ChannelType.TEXT)) {
                engine.put("guild", event.getGuild());
                engine.put("member", event.getMember());
            }

            Object out = engine.eval(
                    "function sendMsg(msg) {" +
                            "channel.sendMessage(msg).queue();" +
                        "}" +
                        "(function() {" +
                            "with (imports) {" +
                                event.getMessage().getRawContent().substring(event.getMessage().getRawContent().split(" ")[0].length()).replaceAll("getToken", "getName") +
                            "}" +
                        "})();");
           sendMsg(event, out == null ? "Executed without error." : out.toString().replaceAll(event.getJDA().getToken(), "Not Today"));
        }
        catch (Exception e) {
            event.getChannel().sendMessage(e.getMessage()).queue();
            e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "A simple eval command (Inspired off of yuis one)";
    }
}
