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

package ml.duncte123.skybot.commands.image;

import me.duncte123.botCommons.messaging.MessageUtils;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class LinusCommand  extends ImageCommandBase {

    @SuppressWarnings("Duplicates")
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (!doAllChecksButNotTheArgsBecauseWeDontNeedThem(event)) {
            return;
        }

        String url = event.getAuthor().getEffectiveAvatarUrl().replace("gif", "png") + "?size=512";

        if (args.size() > 0 && ctx.getMessage().getMentionedUsers().size() < 1) {
            try {
                url = new URL(args.get(0)).toString();
            } catch (MalformedURLException ignored) {
                MessageUtils.sendMsg(event, "That does not look like a valid url");
                return;
            }
        }

        if(ctx.getMessage().getMentionedUsers().size() > 0) {
            url = ctx.getMessage().getMentionedUsers().get(0)
                    .getEffectiveAvatarUrl().replace("gif", "png") + "?size=512";
        }

        ctx.getBlargbot().getLinus(url).async((image) -> handleBasicImage(event, image));
    }

    @Override
    public String help() {
        return "Shows a picture of Linus pointing to something on a monitor.\n" +
                "Usage: `db!linus [image url]`";
    }

    @Override
    public String getName() {
        return "linus";
    }
}