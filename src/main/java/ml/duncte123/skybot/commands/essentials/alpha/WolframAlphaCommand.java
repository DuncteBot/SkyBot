package ml.duncte123.skybot.commands.essentials.alpha;

import com.wolfram.alpha.WAEngine;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class WolframAlphaCommand extends Command {

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        WAEngine engine = AirUtils.alphaEngine;
        engine.toString();
    }

    @Override
    public String help() {
        return "Query Wolfram|Alpha with all your geeky questions";
    }

    @Override
    public String getName() {
        return "alpha";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"wolfram", "wa", "wolframalpha"};
    }
}
