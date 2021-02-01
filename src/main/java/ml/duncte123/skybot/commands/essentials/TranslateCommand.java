/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class TranslateCommand extends Command {

    public TranslateCommand() {
        this.requiresArgs = true;
        this.requiredArgCount = 2;
        this.category = CommandCategory.UTILS;
        this.name = "translate";
        this.help = "Translate a text from English to another language";
        this.usage = "<destination language> <text>";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final String targetLang = args.get(0);
        final String input = String.join(" ", args.subList(1, args.size()));
        final ArrayNode translatedJson = WebUtils.ins.translate("auto", targetLang, input);

        if (translatedJson.isEmpty()) {
            sendMsg(ctx, "No translation found");
            return;
        }

        final String translation = translatedJson.get(0).asText();
        final String message = "Original: " + input + "\n" +
            "Translation to " + targetLang + " : " + translation;

        sendMsg(ctx, message);
    }
}
