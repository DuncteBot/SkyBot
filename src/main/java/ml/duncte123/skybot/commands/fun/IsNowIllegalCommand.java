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
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.MessageUtils;
import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class IsNowIllegalCommand extends Command {

    @Override
    public void executeCommand(CommandContext ctx) {
        String input = ctx.getRawArgs()
                .replaceAll("([^a-zA-Z0-9 ]+)", "").toUpperCase();
        if (input.length() < 1) {
            MessageUtils.sendMsg(ctx.getEvent(), "This command requires a text argument.");
            return;
        }
        if (input.length() > 10)
            input = input.substring(0, 9);
        JSONObject jsonData = new JSONObject().put("task", "gif").put("word", input.replaceAll(" ", "%20"));
        MessageUtils.sendMsg(ctx.getEvent(), "Checking if \"" + input + "\" is illegal....... (might take up to 1 minute)", success ->
                WebUtils.ins.postJSON("https://is-now-illegal.firebaseio.com/queue/tasks.json", jsonData,
                        r -> Objects.requireNonNull(r.body()).string()).async(txt ->
                        commandService.schedule(() -> {

                            String rawJson = getFileJSON(jsonData.getString("word"));

                            if ("null".equals(rawJson)) {
                                success.editMessage(jsonData.getString("word") + " is legal").queue();
                            }
                            JSONObject j = new JSONObject(rawJson);
                            success.editMessage(j.getString("url").replaceAll(" ", "%20")).queue();

                        }, 10L, TimeUnit.SECONDS)
                )
        );
    }

    @Override
    public String help() {
        return "Makes sure that things are illegal.\n" +
                "Usage: `" + PREFIX + getName() + " <words>`";
    }

    @Override
    public String getName() {
        return "isnowillegal";
    }

    private String getFileJSON(String word) {
        return WebUtils.ins.getText("https://is-now-illegal.firebaseio.com/gifs/" + word + ".json").execute();
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }
}
