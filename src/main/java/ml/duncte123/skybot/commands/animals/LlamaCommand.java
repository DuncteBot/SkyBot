package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.URLConnectionReader;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

public class LlamaCommand extends Command {
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        return true;
    }

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
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + e.getMessage())).queue();
        }
    }

    @Override
    public String help() {
        return "Here is a llama";
    }
}
