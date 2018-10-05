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

package ml.duncte123.skybot.commands.fun;

import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class UrbanCommand extends Command {

    public UrbanCommand() {
        this.category = CommandCategory.NSFW;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        if (ctx.getArgs().size() < 1) {
            sendMsg(ctx.getEvent(), "Correct usage: `" + PREFIX + getName() + " <search term>`");
            return;
        }

        String term = ctx.getArgsRaw();
        String url = "http://api.urbandictionary.com/v0/define?term=" + term;
//        String webUrl = "https://www.urbandictionary.com/define.php?term=" + term;
        WebUtils.ins.getJSONObject(url).async(json -> {
//            System.out.println(json);
            if (json.getJSONArray("list").length() < 1) {
                sendMsg(ctx.getEvent(), "Nothing found");
                return;
            }

            JSONObject item = json.getJSONArray("list").getJSONObject(0);
            String permaLink = item.getString("permalink");

            EmbedBuilder eb = EmbedUtils.defaultEmbed()
//                    .setTitle("term", webUrl)
                .setAuthor("Author: " + item.getString("author"))
                .setDescription("_TOP DEFINITION:_\n\n")
                .appendDescription(item.getString("definition"))
                .appendDescription("\n\n")
                .addField("Example", item.getString("example"), false)
                .addField("Upvotes:", item.getInt("thumbs_up") + "", true)
                .addField("Downvotes:", item.getInt("thumbs_down") + "", true)
                .addField("Link:", "[" + permaLink + "](" + permaLink + ")", false);
            sendEmbed(ctx.getEvent(), eb.build());
        });

    }

    @Override
    public String help() {
        return "Search the urban dictionary.\n" +
            "Usage: `" + PREFIX + getName() + " <search term>`";
    }

    @Override
    public String getName() {
        return "urban";
    }
}
