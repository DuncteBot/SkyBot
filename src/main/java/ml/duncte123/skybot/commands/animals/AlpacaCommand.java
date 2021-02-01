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

package ml.duncte123.skybot.commands.animals;

import com.fasterxml.jackson.databind.JsonNode;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.EmbedUtils.embedImage;
import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class AlpacaCommand extends Command {

    public AlpacaCommand() {
        this.category = CommandCategory.ANIMALS;
        this.name = "alpaca";
        this.help = "Shows an alpaca";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        // https://apis.duncte123.me/animal/alpaca
        final JsonNode json = ctx.getApis().getAnimal("alpaca");

        sendEmbed(ctx, embedImage(json.get("file").asText()));
    }
}
