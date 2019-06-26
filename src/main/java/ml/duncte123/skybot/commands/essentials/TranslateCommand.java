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

package ml.duncte123.skybot.commands.essentials;

import com.fasterxml.jackson.databind.node.ArrayNode;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "sylmoss", author = "Sylvia Moss")
public class TranslateCommand extends Command {

    public TranslateCommand() {
        this.category = CommandCategory.UTILS;
    }

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();

        if (args.isEmpty() || args.size() < 2) {
            sendMsg(event, "Correct usage: `" + ctx.getPrefix() + getName() + " <destination language code> <text>`");
            return;
        }

        final String targetLang = args.get(0);
        final String input = String.join(" ", args.subList(1, args.size()));
        final ArrayNode translatedJson = WebUtils.ins.translate("auto", targetLang, input);

        if (translatedJson.size() < 1) {
            sendMsg(ctx.getEvent(), "No translation found");
            return;
        }

        final String translation = translatedJson.get(0).asText();
        final String message = "Original: " + input + "\n" +
            "Translation to " + targetLang + " : " + translation;

        sendMsg(event, message);
    }

    @Override
    public String getName() {
        return "translate";
    }

    @Override
    public String help(String prefix) {
        return "Translate a text from English to another language\n"
            + "Usage: `" + prefix + getName() + " <destination language> <text>`";
    }
}
