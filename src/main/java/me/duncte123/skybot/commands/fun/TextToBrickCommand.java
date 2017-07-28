package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.utils.AirUtils;
import org.apache.commons.lang3.StringUtils;
import me.duncte123.skybot.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Created by Duncan on 9-7-2017.
 */
public class TextToBrickCommand implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        if (args.length == 0) {
            event.getChannel().sendMessage("Please type some words").queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
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

    @Override
    public void executed(boolean safe, MessageReceivedEvent event) {
      return;
    }
}
