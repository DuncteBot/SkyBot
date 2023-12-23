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

package me.duncte123.skybot.commands.animals;

import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.skybot.objects.command.Command;
import me.duncte123.skybot.objects.command.CommandCategory;
import me.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class LlamaCommand extends Command {

    public LlamaCommand() {
        this.category = CommandCategory.ANIMALS;
        this.name = "llama";
        this.help = "Shows a llama";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        // https://apis.duncte123.me/animal/llama
        final JsonNode json = ctx.getApis().getAnimal("llama");

        sendEmbed(ctx, EmbedUtils.embedImage(json.get("file").asText()));
    }
}
