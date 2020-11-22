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

package ml.duncte123.skybot.commands.image;

import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class PornHubCommand extends ImageCommandBase {

    public PornHubCommand() {
        this.name = "pornhub";
        this.help = "Generates a pornhub logo";
        this.usage = "<text1>|<text2>";
    }

    @Override
    public void execute(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (!passes(event)) {
            return;
        }

        final String[] split = splitString(ctx);

        if (split == null) {
            return;
        }

        if (split[0].length() > 200 || split[1].length() > 200) {
            sendMsg(ctx, "Please limit your input to 200 characters to either side of the bar");

            return;
        }

        ctx.getAlexFlipnote().getPornhub(split[0], split[1]).async((image) -> handleBasicImage(event, image));
    }
}
