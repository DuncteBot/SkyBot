package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Duncan on 9-7-2017.
 */
public class TextToBrickCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if (args.length < 1) {
            event.getChannel().sendMessage("Correct usage: `"+ Settings.prefix + getName() +" <words>`").queue();
            return;
        }

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

        sendEmbed(event, EmbedUtils.embedMessage( sb.toString() ));
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Text to bricks fun.";
    }

    @Override
    public String getName() {
        return "ttb";
    }
}
