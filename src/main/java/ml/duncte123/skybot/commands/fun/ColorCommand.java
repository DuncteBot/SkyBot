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

package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import static java.awt.Color.decode;
import static me.duncte123.botcommons.messaging.EmbedUtils.defaultEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class ColorCommand extends Command {

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        ctx.getAlexFlipnote().getRandomColour().async((data) -> {
            String hex = data.hex;
            String image = data.image;
            int integer = data.integer;
            int brightness = data.brightness;
            String name = data.name;
            String rgb = data.rgb;

            EmbedBuilder embed = defaultEmbed()
                .setColor(decode(hex))
                .setThumbnail(image);

            String desc = String.format("Name: %s%nHex: %s%nInt: %s%nRGB: %s%nBrightness: %s",
                name, hex, integer, rgb, brightness);
            embed.setDescription(desc);

            sendEmbed(ctx.getEvent(), embed.build());
        });
    }

    @Override
    public String help() {
        return "Shows a random colour.";
    }

    @Override
    public String getName() {
        return "colour";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"color"};
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }
}
