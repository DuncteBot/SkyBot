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
import net.dv8tion.jda.core.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import static java.awt.Color.decode;
import static ml.duncte123.skybot.BuildConfig.URL_ARRAY;
import static ml.duncte123.skybot.utils.EmbedUtils.defaultEmbed;
import static ml.duncte123.skybot.utils.MessageUtils.sendEmbed;

public class ColorCommand extends Command {

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        WebUtils.ins.getJSONObject(URL_ARRAY[2] + "/random").async((json) -> {
            String hex = json.getString("hex");
            String image = json.getString("image");
            int integer = json.getInt("int");
            String name = json.getString("name");
            String rgb = json.getString("rgb");

            EmbedBuilder embed = defaultEmbed()
                    .setColor(decode(hex))
                    .setThumbnail(image);

            String desc = String.format("Name(s): %s%nHex: %s%nInt: %s%nRGB: %s", name, hex, integer, rgb);
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
