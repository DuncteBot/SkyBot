package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Duncan on 9-7-2017.
 */
public class TextToBrickCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {

        if (args.length == 0) {
            event.getChannel().sendMessage("Please type some words").queue();
            return false;
        }

        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
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

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Text to bricks fun.";
    }
}
