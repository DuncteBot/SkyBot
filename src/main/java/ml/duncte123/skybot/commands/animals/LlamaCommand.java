package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.URLConnectionReader;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONObject;

public class LlamaCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        try {
            String theLlama = (args.length<1 ? "random" : args[0]);
            String jsonString = URLConnectionReader.getText("https://lucoa.systemexit.co.uk/animals/api/llama/" + theLlama);
            JSONObject jsonObject = new JSONObject(jsonString);
            event.getChannel().sendMessage(AirUtils.embedImage(jsonObject.getString("file"))).queue();
        }
        catch (Exception e) {
            e.printStackTrace();
            sendMsg(event, AirUtils.embedMessage("ERROR: " + e.getMessage()));
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Here is a llama";
    }
}
