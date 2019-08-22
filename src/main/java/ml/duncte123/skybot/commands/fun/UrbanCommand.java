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

import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class UrbanCommand extends Command {

    public UrbanCommand() {
        this.category = CommandCategory.NSFW;
        this.name = "urban";
        this.helpFunction = (invoke, prefix) -> "Searches the urban dictionary";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <search term>`";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {

        if (ctx.getArgs().isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final String term = ctx.getArgsRaw();
        final String url = "http://api.urbandictionary.com/v0/define?term=" + term;
//        String webUrl = "https://www.urbandictionary.com/define.php?term=" + term;
        WebUtils.ins.getJSONObject(url).async((json) -> {
            if (json.get("list").size() < 1) {
                sendMsg(ctx.getEvent(), "Nothing found");
                return;
            }

            final JsonNode item = json.get("list").get(0);
            final String permaLink = item.get("permalink").asText();

            final EmbedBuilder eb = EmbedUtils.defaultEmbed()
//                    .setTitle("term", webUrl)
                .setAuthor("Author: " + item.get("author").asText())
                .setDescription("_TOP DEFINITION:_\n\n")
                .appendDescription(item.get("definition").asText())
                .appendDescription("\n\n")
                .addField("Example", item.get("example").asText(), false)
                .addField("Upvotes:", item.get("thumbs_up").asInt() + "", true)
                .addField("Downvotes:", item.get("thumbs_down").asInt() + "", true)
                .addField("Link:", "[" + permaLink + "](" + permaLink + ")", false);
            sendEmbed(ctx.getEvent(), eb.build());
        });

    }
}
