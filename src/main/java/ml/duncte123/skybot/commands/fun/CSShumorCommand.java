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

package ml.duncte123.skybot.commands.fun;

import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;

import static ml.duncte123.skybot.BuildConfig.URL_ARRAY;

public class CSShumorCommand extends Command {
    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {
        WebUtils.ins.scrapeWebPage(URL_ARRAY[0]).async( (doc) -> {
            Element code = doc.selectFirst(".crayon-pre");
            String text = code.text()
                .replace("*/ ", "*/\n") // Newline + tab after comments
                .replace("{ ", "{\n\t") // Newline + tab after {
                .replaceAll("; [^}]", ";\n\t") // Newline + tab after '; (not })'
                .replace("; }", ";\n}");
            String message = String.format("```CSS\n%s```", text);
            MessageUtils.sendEmbed(event, EmbedUtils.embedMessage(message));
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
}
