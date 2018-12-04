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
import me.duncte123.botcommons.web.WebUtils.EncodingType;
import me.duncte123.botcommons.web.WebUtilsErrorUtils;
import me.duncte123.weebJava.helpers.QueryBuilder;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class YodaSpeakCommand extends Command {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();

        if (ctx.getArgs().isEmpty()) {
            sendMsg(event, "Correct usage: `" + PREFIX + getName() + " <A sentence.>`");
            return;
        }

        QueryBuilder builder = new QueryBuilder()
            .append("https://apis.duncte123.me/yoda")
            .append("sentence", ctx.getArgsDisplay())
            .append("token", event.getJDA().getToken());

        WebUtils.ins.prepareRaw(WebUtils.defaultRequest()
            .url(builder.build())
            .addHeader("Accept", EncodingType.APPLICATION_JSON.getType())
            .build(), WebUtilsErrorUtils::toJSONObject).async(
            (json) -> {
                logger.debug("Yoda response: " + json);
                sendMsg(event, json.getString("data"));
            },
            error -> {
                error.printStackTrace();
                sendMsg(event, "Yoda is asleep tell my developers to wake him up");
            }
        );

    }

    @Override
    public String help() {
        return "Convert your sentences into yoda speak.\n" +
            "Usage: `" + PREFIX + getName() + " <A sentence.>`";
    }

    @Override
    public String getName() {
        return "yoda";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }
}
