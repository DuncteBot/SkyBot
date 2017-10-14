package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;

public class BirbCommand extends Command {
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        try {
            String imgName = WebUtils.getText("https://proximyst.com:4500/random/path/text");

            sendEmbed(event, EmbedUtils.embedImage("https://proximyst.com:4500/image/" + imgName + "/image"));
        }
        catch (IOException e) {
            sendMsg(event, "ERROR: " + e.getMessage());
        }
    }

    @Override
    public String help() {
        return "Here is a Bitb";
    }

    @Override
    public String getName() {
        return "birb";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"bird"};
    }
}
