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

package ml.duncte123.skybot.commands.uncategorized;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class YesNoCommand extends Command {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        WebUtils.ins.getJSONObject("https://yesno.wtf/api").async((it) ->
            sendEmbed(ctx.getEvent(), EmbedUtils.embedImageWithTitle(
                it.getString("answer"),
                it.getString("image"),
                it.getString("image"))
            )
        );
    }


    @Override
    public String getName() {
        return "yesno";
    }

    @Override
    public String help() {
        return "Chooses between yes or no\n" +
            "Usage: `" + Settings.PREFIX + getName() + '`';
    }
}
