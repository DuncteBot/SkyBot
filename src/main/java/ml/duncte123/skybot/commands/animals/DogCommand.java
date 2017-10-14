package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class DogCommand extends Command {

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        String base = "https://random.dog/";
        try {
            String jsonString = WebUtils.getText(base + "woof");
            String finalS = base + jsonString;

            if (finalS.contains(".mp4")) {
               sendEmbed(event, EmbedUtils.embedField("A video", "[OMG LOOK AT THIS CUTE VIDEO](" + finalS + ")"));
            } else {
                sendEmbed(event, EmbedUtils.embedImage(finalS));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sendEmbed(event, EmbedUtils.embedMessage("**[OOPS]** Something broke, blame duncte"));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "here is a dog.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "dog";
    }
}
