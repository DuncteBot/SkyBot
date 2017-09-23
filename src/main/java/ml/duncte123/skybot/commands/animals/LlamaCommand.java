package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.URLConnectionReader;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONObject;

public class LlamaCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        try {
            String theLlama = (args.length<1 ? "random" : args[0]);
            String jsonString = URLConnectionReader.getText("https://api.systemexit.co.uk/animals/llama/" + theLlama);
            JSONObject jsonObject = new JSONObject(jsonString);
            event.getChannel().sendMessage(EmbedUtils.embedImage(jsonObject.getString("file"))).queue();
        }
        catch (Exception e) {
            e.printStackTrace();
            sendEmbed(EmbedUtils.embedMessage("ERROR: " + e.getMessage()), event);
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

    @Override
    public String getName() {
        return "llama";
    }
}
