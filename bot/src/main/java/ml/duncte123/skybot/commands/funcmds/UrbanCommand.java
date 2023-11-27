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

package ml.duncte123.skybot.commands.funcmds;

import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class UrbanCommand extends Command {

    public UrbanCommand() {
        this.requiresArgs = true;
        this.category = CommandCategory.NSFW;
        this.name = "urban";
        this.help = "Searches the urban dictionary";
        this.usage = "<search term>";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final String term = ctx.getArgsRaw();
        final String url = "http://api.urbandictionary.com/v0/define?term=" + term;
//        String webUrl = "https://www.urbandictionary.com/define.php?term=" + term;
        WebUtils.ins.getJSONObject(url).async((json) -> {
            if (json.get("list").isEmpty()) {
                sendMsg(ctx, "Nothing found");
                return;
            }

            final JsonNode item = json.get("list").get(0);
            final String permaLink = item.get("permalink").asText();

            final EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
//                    .setTitle("term", webUrl)
                .setAuthor("Author: " + item.get("author").asText())
                .setDescription("_TOP DEFINITION:_\n\n")
                .appendDescription(item.get("definition").asText())
                .appendDescription("\n\n")
                .addField("Example", item.get("example").asText(), false)
                .addField("Upvotes:", String.valueOf(item.get("thumbs_up").asInt()), true)
                .addField("Downvotes:", String.valueOf(item.get("thumbs_down").asInt()), true)
                .addField("Link:", "[" + permaLink + "](" + permaLink + ")", false);
            sendEmbed(ctx, builder);
        });

    }
}
