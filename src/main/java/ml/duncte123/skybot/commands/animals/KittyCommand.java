/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.net.URL;

public class KittyCommand extends Command {

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        try {
            String apiKey = AirUtils.config.getString("apis.thecatapi", "");
            Document raw = Jsoup.connect("http://thecatapi.com/api/images/get?" +
                    (!apiKey.isEmpty()? "api_key=" + apiKey + "&" : "") + "format=xml&results_per_page=1").get();
            Document doc = Jsoup.parse(raw.getAllElements().html(), "", Parser.xmlParser());

            String fullUrl = doc.select("url").first().text();
            String fileName = fullUrl.split("/")[3];

            event.getChannel().sendFile(new URL(fullUrl).openStream(),
                    fileName, null).queue();
        }
        catch (Exception e) {
            sendEmbed(event, EmbedUtils.embedMessage("ERROR: " + e.getCause().toString()));
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "A alternative cat command with more kitties";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "kitty";
    }
}
