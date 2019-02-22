/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.commands.fun;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;

import java.util.Map;
import java.util.TreeMap;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static ml.duncte123.skybot.utils.EarthUtils.sendRedditPost;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class CSShumorCommand extends Command {

    private final TLongIntMap cssIndex = new TLongIntHashMap();

    public CSShumorCommand() {
        this.category = CommandCategory.FUN;
    }


    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        switch (ctx.getRandom().nextInt(2)) {
            case 1:
                sendRedditPost("css_irl", cssIndex, ctx.getEvent(), true);
                break;
            default:
                sendCssJoke(ctx.getEvent());
                break;
        }

    }

    @Author(nickname = "ramidzkh", author = "Ramid Khan") // Thanks for the regex bud
    private void sendCssJoke(GuildMessageReceivedEvent event) {
        WebUtils.ins.scrapeWebPage("https://csshumor.com/").async((doc) -> {
            final Element code = doc.selectFirst(".crayon-pre");
            final String text = code.text()
                .replace("*/ ", "*/\n") // Newline + tab after comments
                .replace("{ ", "{\n\t") // Newline + tab after {
                .replaceAll("; ([^}])", ";\n\t$1") // Newline + tab after '; (not })'
                .replace("; }", ";\n}");
            final String message = String.format("```CSS\n%s```", text);
            final Element link = doc.selectFirst(".funny h2 a");
            sendEmbed(event, EmbedUtils.defaultEmbed()
                .setTitle(link.text(), link.attr("href"))
                .setDescription(message)
                .build());
        });
    }

    @Override
    public String help() {
        return "Gives you a funny css joke";
    }

    @Override
    public String getName() {
        return "csshumor";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"cssjoke"};
    }
}
