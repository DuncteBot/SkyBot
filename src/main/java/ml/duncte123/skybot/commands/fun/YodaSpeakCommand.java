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
import me.duncte123.botCommons.web.WebUtils.EncodingType;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import static ml.duncte123.skybot.utils.MessageUtils.sendMsg;

public class YodaSpeakCommand extends Command {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (args.size() < 1) {
            sendMsg(event, "Correct usage: `" + PREFIX + getName() + " <A sentence.>`");
            return;
        }

        WebUtils.ins.prepareRaw(WebUtils.defaultRequest()
                .url("https://apis.duncte123.me/yoda?sentence=" + StringUtils.join(args, "+"))
                .addHeader("X-User-id", event.getJDA().getSelfUser().getId())
                .addHeader("X-client-token", event.getJDA().getToken())
                .addHeader("Accept", EncodingType.APPLICATION_JSON.getType())
                .build(), Response::body).async(
                (body) -> {
                    try {
                        final JSONObject json = new JSONObject(body.string());
                        logger.debug("Yoda response: " + json);
                        sendMsg(event, json.getString("sentence"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendMsg(event, "Yoda is asleep tell my developers to wake him up");
                    }
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
