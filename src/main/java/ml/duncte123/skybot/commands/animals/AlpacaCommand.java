package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;

public class AlpacaCommand extends Command {

    /**
     * {@inheritDoc}
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
            sendEmbed(event, EmbedUtils.embedMessage("ERROR: " + e.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Here is a alpaca";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "alpaca";
    }
}
