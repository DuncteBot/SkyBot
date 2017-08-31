package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.URLConnectionReader;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class DogCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        String base = "https://random.dog/";
        try {
            String jsonString = URLConnectionReader.getText(base + "woof");
            String finalS = base + jsonString;

            if (finalS.contains(".mp4")) {
               sendMsg(event, AirUtils.embedField("A video", "[OMG LOOK AT THIS CUTE VIDEO](" + finalS + ")"));
            } else {
                sendMsg(event, AirUtils.embedImage(finalS));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            event.getChannel().sendMessage(AirUtils.embedMessage("**[OOPS]** Something broke, blame duncte")).queue();
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
}
