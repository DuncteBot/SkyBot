package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;

public class AlpacaCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        try {
            Document doc = Jsoup.connect("http://www.randomalpaca.com/").get();

            Element img = doc.select("img").first();
            event.getChannel().sendFile(new URL(img.attributes().get("src")).openStream(), "Alpaca_" + System.currentTimeMillis() + ".png", null).queue();
        }
        catch (Exception e) {
            e.printStackTrace();
            sendEmbed(AirUtils.embedMessage("ERROR: " + e.getMessage()), event);
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Here is a alpaca";
    }

    @Override
    public String getName() {
        return "alpaca";
    }
}
