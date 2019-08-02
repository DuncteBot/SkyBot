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

package ml.duncte123.skybot.commands.animals;

import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SealCommand extends Command {

    public SealCommand() {
        this.category = CommandCategory.ANIMALS;
        this.name = "seal";
        this.aliases = new String[]{
            "zeehond",
        };
        this.helpFunction = (invoke, prefix) -> "Shows a seal";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        // https://apis.duncte123.me/animal/seal
        final JsonNode data = ctx.getApis().executeDefaultGetRequest("seal", false).get("data");

        sendEmbed(ctx.getEvent(), EmbedUtils.embedImage(data.get("file").asText()));
    }
}
