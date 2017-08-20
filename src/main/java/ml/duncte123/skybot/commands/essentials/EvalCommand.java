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
        try
        {
            engine.eval("var imports = new JavaImporter(" +
                    "java.io," +
                    "java.lang," +
                    "java.util," +
                    "Packages.net.dv8tion.jda.core," +
                    "Packages.net.dv8tion.jda.core.entities," +
                    "Packages.net.dv8tion.jda.core.entities.impl," +
                    "Packages.net.dv8tion.jda.core.managers," +
                    "Packages.net.dv8tion.jda.core.managers.impl," +
                    "Packages.net.dv8tion.jda.core.utils);");
        }
        catch (ScriptException e)
        {
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
            engine.put("api", event.getJDA());
            engine.put("commands", SkyBot.commands);
            if (event.isFromType(ChannelType.TEXT)) {
                engine.put("guild", event.getGuild());
                engine.put("member", event.getMember());
            }

            Object out = engine.eval(
                    "(function() {" +
                            "with (imports) {" +
                            event.getMessage().getContent().substring(event.getMessage().getContent().split(" ")[0].length()) +
                            "}" +
                            "})();");
            event.getChannel().sendMessage(out == null ? "Executed without error." : out.toString()).queue();
        }
        catch (ScriptException e1) {
            event.getChannel().sendMessage(e1.getMessage()).queue();
        }
    }

    @Override
    public String help() {
        return null;
    }
}
