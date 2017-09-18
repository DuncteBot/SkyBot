package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.URLConnectionReader;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class DogCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        String base = "https://random.dog/";
        try {
            String jsonString = URLConnectionReader.getText(base + "woof");
            String finalS = base + jsonString;

            if (finalS.contains(".mp4")) {
               sendEmbed(AirUtils.embedField("A video", "[OMG LOOK AT THIS CUTE VIDEO](" + finalS + ")"), event);
            } else {
                sendEmbed(AirUtils.embedImage(finalS), event);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sendEmbed(AirUtils.embedMessage("**[OOPS]** Something broke, blame duncte"), event);
        }

    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "here is a dog.";
    }

    @Override
    public String getName() {
        return "dog";
    }
}
