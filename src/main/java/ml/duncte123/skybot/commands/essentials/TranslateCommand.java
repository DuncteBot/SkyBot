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

package ml.duncte123.skybot.commands.essentials;

import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "sylmoss", author = "Sylvia Moss")
public class TranslateCommand extends Command {

    public TranslateCommand() {
        this.category = CommandCategory.NERD_STUFF;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (args.isEmpty() || args.size() < 2) {
            sendMsg(event, "Correct usage: `" + PREFIX + getName() + "[destination language code] <text>`");
            return;
        }

        String targetLang = args.get(0);
        String input = String.join(" ", args.subList(1, args.size() - 1));
        JSONArray translatedJson = WebUtils.ins.translate("auto", targetLang, input);

        if (translatedJson.length() < 1) {
            sendMsg(ctx.getEvent(), "No translation found");
            return;
        }

        String translation = translatedJson.getString(0);
        String message = "Original: " + input + "\n" +
            "Translation to " + targetLang + " : " + translation;
        sendMsg(event, message);
    }

    @Override
    public String getName() {
        return "translate";
    }

    @Override
    public String help() {
        return "Translate a text from English to another language\n"
            + "Usage: `" + PREFIX + getName() + "[destination language] <text>";
    }
}
