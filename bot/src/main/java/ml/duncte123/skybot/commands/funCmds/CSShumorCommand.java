/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.funCmds;

import gnu.trove.map.TLongIntMap;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.MapUtils;

import javax.annotation.Nonnull;

import static ml.duncte123.skybot.utils.EarthUtils.sendRedditPost;

public class CSShumorCommand extends Command {

    private final TLongIntMap cssIndex = MapUtils.newLongIntMap();

    public CSShumorCommand() {
        this.category = CommandCategory.FUN;
        this.name = "csshumor";
        this.aliases = new String[]{
            "cssjoke",
        };
        this.help = "Sends a css-related joke";
    }


    @Override
    public void execute(@Nonnull CommandContext ctx) {
        sendRedditPost("css_irl", cssIndex, ctx, true);

        // broken shit :D
        /*if (ctx.getRandom().nextInt(2) == 1) {
            sendRedditPost("css_irl", cssIndex, ctx, true);
        } else {
            sendCssJoke(ctx);
        }*/
    }

    // Thanks for the regex ramidzkh
//    private void sendCssJoke(CommandContext ctx) {
//        WebUtils.ins.scrapeWebPage("https://csshumor.com/").async((doc) -> {
//            final Element code = doc.selectFirst(".crayon-pre");
//            final String text = code.text()
//                .replace("*/ ", "*/\n") // Newline + tab after comments
//                .replace("{ ", "{\n\t") // Newline + tab after {
//                .replaceAll("; ([^}])", ";\n\t$1") // Newline + tab after '; (not })'
//                .replace("; }", ";\n}");
//            final String message = String.format("```CSS\n%s```", text);
//            final Element link = doc.selectFirst(".funny h2 a");
//            sendEmbed(ctx, EmbedUtils.getDefaultEmbed()
//                .setTitle(link.text(), link.attr("href"))
//                .setDescription(message));
//        });
//    }
}
