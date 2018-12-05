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

package ml.duncte123.skybot.commands.guild.owner.settings;

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetColorCommand extends SettingsBase {

    private static final Pattern COLOR_REGEX = Pattern.compile("#[a-zA-Z0-9]{6}");

    @Override
    public void run(@NotNull CommandContext ctx) {

        List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            sendMsg(ctx.getEvent(), "Correct usage: `" + PREFIX + getName() + " <hex color>`");
            return;
        }

        String colorString = args.get(0);
        Matcher colorMatcher = COLOR_REGEX.matcher(colorString);

        if (!colorMatcher.matches()) {
            sendMsg(ctx.getEvent(), "That color does not look like a valid hex color, hex colors start with a pound sign.\n" +
                "Tip: you can use <http://colorpicker.com/> to generate a hex code.");
            return;
        }

        int colorInt = Color.decode(colorString).getRGB();

        ctx.getGuild().setColor(colorInt);

        String msg = String.format("Embed color has been set to `%s`", colorString);
        sendEmbed(ctx.getEvent(), EmbedUtils.embedMessage(msg));
    }

    @Override
    public String getName() {
        return "setcolor";
    }

    @Override
    public String help() {
        return "Sets the colors of the embeds from the bot.\n" +
            "Usage: `" + PREFIX + getName() + " <hex color>`";
    }
}
