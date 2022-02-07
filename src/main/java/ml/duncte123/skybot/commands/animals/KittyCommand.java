/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.animals;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.objects.BaseCommand;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class KittyCommand extends BaseCommand {

    public KittyCommand() {
        super(
            "cat",
            "Shows a random cat",
            CommandCategory.ANIMALS,
            null,
            new String[]{
                "kitty",
            }
        );
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final String apiKey = ctx.getConfig().apis.thecatapi;
        final String url = "https://api.thecatapi.com/v1/images/search?limit=1";

        WebUtils.ins.getJSONArray(url, null, (req) -> req.header("x-api-key", apiKey)).async((json) -> {
            sendEmbed(ctx, EmbedUtils.embedImage(
                json.get(0).get("url").asText()
            ));
        });
    }
}
