package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Duncan on 9-7-2017.
 */
public class TextToBrickCommand extends Command {

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {

        if (args.length == 0) {
            event.getChannel().sendMessage("Please type some words").queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
            StringBuilder sb = new StringBuilder();
            for (String a : StringUtils.join(args, " ").split("")) {
                if (Character.isLetter(a.toLowerCase().charAt(0))) {
                    sb.append(":regional_indicator_").append(a.toLowerCase()).append(":");
                } else {
                    if (" ".equals(a)) {
                        sb.append(" ");
                    }
                    sb.append(a);
                }
            }

        event.getChannel().sendMessage(AirUtils.embedMessage( sb.toString() )).queue();
    }

    @Override
    public String help() {
        return "Text to bricks fun.";
    }
}
