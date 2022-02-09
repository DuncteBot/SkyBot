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

package ml.duncte123.skybot.commands.fun;

import kotlin.Pair;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class ExplosmCommand extends Command {
    public ExplosmCommand() {
        this.category = CommandCategory.FUN;
        this.name = "explosm";
        this.aliases = new String[] {
            "explosmrcg",
            "rcg"
        };
        this.help = "Generates a random comic using the Random Comic Generator on explosm.net";
        this.botPermissions = new Permission[] {
            Permission.MESSAGE_EMBED_LINKS,
        };
    }

    @Override
    public void execute(@NotNull CommandContext ctx) {
        if (!ctx.getChannel().isNSFW()) {
            sendMsg(ctx, "Due to the nature of explosm comics this command is restricted to nsfw channels");
            return;
        }

        final Pair<String, String> rcgParts = ctx.getApis().getRCGUrl();

        if (rcgParts == null) {
            sendMsg(ctx, "Generating comic failed, try again later");
            return;
        }

        sendEmbed(ctx, EmbedUtils.embedImageWithTitle(
            "Click here to share",
            rcgParts.getSecond(),
            rcgParts.getFirst()
        ));
    }
}
