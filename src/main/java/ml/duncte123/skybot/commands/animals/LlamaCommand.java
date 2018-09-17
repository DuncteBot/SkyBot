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

package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.api.LlamaObject;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.ApiUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import org.jetbrains.annotations.NotNull;

import static ml.duncte123.skybot.utils.MessageUtils.sendEmbed;

public class LlamaCommand extends Command {

    public LlamaCommand() {
        this.category = CommandCategory.ANIMALS;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        LlamaObject llama = ApiUtils.getRandomLlama(ctx.getDatabase());

        sendEmbed(ctx.getEvent(), EmbedUtils.embedImage(llama.getFile()));

        /*WebUtils.ins.getJSONObject(Settings.OLD_API_BASE + "/llama/json").async(
                (json) -> MessageUtils.sendEmbed(event, EmbedUtils.embedImage(json.getString("file")))
        );*/
    }

    @Override
    public String help() {
        return "Here is a llama";
    }

    @Override
    public String getName() {
        return "llama";
    }
}
