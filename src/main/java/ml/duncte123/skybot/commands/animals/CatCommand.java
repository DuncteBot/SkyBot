package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.URLConnectionReader;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONObject;

import java.net.URL;

public class CatCommand extends Command {

    public final static String help = "here is a cat.";

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub;

        try {
            String jsonString = URLConnectionReader.getText("http://random.cat/meow");
            JSONObject jsonObject = new JSONObject(jsonString);
            String newJSON = jsonObject.getString("file");
            event.getChannel().sendFile(new URL(newJSON).openStream(), "cat_" + System.currentTimeMillis() + ".png", null).queue();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sendEmbed(event, EmbedUtils.embedMessage("OOPS: " + e.getMessage()));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "cat";
    }
}
