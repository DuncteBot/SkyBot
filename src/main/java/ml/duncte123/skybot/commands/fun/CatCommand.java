package ml.duncte123.skybot.commands.fun;

import java.net.URL;

import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.URLConnectionReader;
import org.json.JSONObject;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CatCommand extends Command {

    public final static String help = "here is a cat.";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub;

        try {
            String jsonString = URLConnectionReader.getText("http://random.cat/meow");
            JSONObject jsonObject = new JSONObject(jsonString);
            String newJSON = jsonObject.getString("file");
            event.getTextChannel().sendFile(new URL(newJSON).openStream(), "cat_" + System.currentTimeMillis() + ".png", null).queue();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            event.getChannel().sendMessage(AirUtils.embedMessage("OOPS: " + e.getMessage())).queue();
        }

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }
}
