package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.AirUtils;
import me.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.net.URL;

public class KittyCommand extends Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        try {
            Document raw = Jsoup.connect("http://thecatapi.com/api/images/get?" +
                    (!Config.catAPIKey.isEmpty()? "api_key=" + Config.catAPIKey + "&" : "") + "format=xml&results_per_page=1").get();
            Document doc = Jsoup.parse(raw.getAllElements().html(), "", Parser.xmlParser());
            event.getTextChannel().sendFile(new URL(doc.select("url").first().text()).openStream(),
                    "Kitty_" + System.currentTimeMillis() + ".png", null).queue();
        }
        catch (Exception e) {
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + e.getMessage())).queue();
            e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "A alternative cat command with more kitties";
    }
}
