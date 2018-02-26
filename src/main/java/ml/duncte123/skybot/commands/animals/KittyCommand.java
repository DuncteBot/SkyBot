/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

public class KittyCommand extends Command {

    public KittyCommand() {
        this.category = CommandCategory.ANIMALS;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        try {
            String apiKey = AirUtils.CONFIG.getString("apis.thecatapi", "");
            Document raw = Jsoup.connect("http://thecatapi.com/api/images/get?" +
                                                 (!apiKey.isEmpty() ? "api_key=" + apiKey + "&" : "") + "format=xml&results_per_page=1").get();
            Document doc = Jsoup.parse(raw.getAllElements().html(), "", Parser.xmlParser());

            String fullUrl = doc.select("url").first().text();

            MessageUtils.sendEmbed(event, EmbedUtils.embedImage(fullUrl));
        } catch (Exception e) {
            MessageUtils.sendEmbed(event, EmbedUtils.embedMessage("ERROR: " + e.toString()));
            //e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "A alternative cat command with more kitties";
    }

    @Override
    public String getName() {
        return "kitty";
    }
}
