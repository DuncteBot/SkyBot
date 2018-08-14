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

import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

public class KittyCommand extends Command {

    public KittyCommand() {
        this.category = CommandCategory.ANIMALS;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        String apiKey = ctx.getConfig().apis.thecatapi;
        WebUtils.ins.getText("http://thecatapi.com/api/images/get?" +
                (!apiKey.isEmpty() ? "api_key=" + apiKey + "&" : "") + "format=xml&results_per_page=1").async((xml) -> {
            Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
            String fullUrl = doc.selectFirst("url").text();
            String sourceUrl = doc.selectFirst("source_url").text();
            MessageUtils.sendEmbed(ctx.getEvent(), EmbedUtils.embedImageWithTitle("source", sourceUrl, fullUrl));
        });
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
