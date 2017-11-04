/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;

public class AlpacaCommand extends Command {

    public AlpacaCommand() {
        this.category = CommandCategory.ANIMALS;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
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

    @Override
    public String help() {
        return "Here is a alpaca";
    }

    @Override
    public String getName() {
        return "alpaca";
    }
}
