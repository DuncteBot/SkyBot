/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.EmbedUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;

import static ml.duncte123.skybot.utils.MessageUtils.sendEmbed;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class AlpacaCommand extends Command {

    public AlpacaCommand() {
        this.category = CommandCategory.ANIMALS;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        WebUtils.ins.scrapeWebPage("http://www.randomalpaca.com/").async((doc) -> {
            Element img = doc.select("img").first();
            sendEmbed(ctx.getEvent(), EmbedUtils.embedImage(img.attributes().get("src")));
        });
    }

    @Override
    public String help() {
        return "Here is an alpaca";
    }

    @Override
    public String getName() {
        return "alpaca";
    }
}
