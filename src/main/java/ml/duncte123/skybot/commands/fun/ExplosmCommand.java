/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2021  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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
    }

    @Override
    public void execute(@NotNull CommandContext ctx) {
        if (!ctx.getChannel().isNSFW()) {
            sendMsg(ctx, "Due to the nature of explosm comics this command is restricted to nsfw channels");
            return;
        }

        this.generateComic((comicUrl) -> {
            if (comicUrl == null) {
                sendMsg(ctx, "Generating comic failed, try again later");
                return;
            }

            sendEmbed(ctx, EmbedUtils.embedImageWithTitle("Fresh comic for you", "https://explosm.net/rcg", comicUrl));
        });
    }

    public void generateComic(Consumer<String> callback) {
        // TODO: port to prod, beta is unstable as fuck
        final Request.Builder builder = WebUtils.ins.prepareGet("https://apis.beta.duncte123.me/images/rcg/random");

        WebUtils.ins.prepareRaw(
            builder.build(),
            (response) -> response.request().url().toString()
        ).async(callback, (e) -> callback.accept(null));
    }
}
