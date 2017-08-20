package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;

public class AlpacaCommand extends Command {
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        try {
            Document doc = Jsoup.connect("http://www.randomalpaca.com/").get();

            Element img = doc.select("img").first();
            event.getChannel().sendFile(new URL(img.attributes().get("src")).openStream(), "Alpaca_" + System.currentTimeMillis() + ".png", null).queue();
        }
        catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + e.getMessage())).queue();
        }
    }

    @Override
    public String help() {
        return "Here is a alpaca";
    }
}
