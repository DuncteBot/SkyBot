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

package ml.duncte123.skybot.commands.guild.owner.settings;

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.AirUtils.colorToInt;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetColorCommand extends SettingsBase {

    private static final Pattern COLOR_REGEX = Pattern.compile("#[a-zA-Z0-9]{6}");

    public SetColorCommand() {
        this.name = "setcolor";
        this.aliases = new String[]{
            "setembedcolor",
        };
        this.helpFunction = (invoke, prefix) -> "Sets the color of the embeds that the bot sends";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <hex color>`";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {

        final List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final String colorString = args.get(0);
        final Matcher colorMatcher = COLOR_REGEX.matcher(colorString);

        if (!colorMatcher.matches()) {
            sendMsg(ctx.getEvent(), "That color does not look like a valid hex color, hex colors start with a pound sign.\n" +
                "Tip: you can use <http://colorpicker.com/> to generate a hex code.");
            return;
        }

        final int colorInt = colorToInt(colorString);

        ctx.getGuild().setColor(colorInt);

        final String msg = String.format("Embed color has been set to `%s`", colorString);

        sendEmbed(ctx.getEvent(), EmbedUtils.embedMessage(msg));
    }
}
