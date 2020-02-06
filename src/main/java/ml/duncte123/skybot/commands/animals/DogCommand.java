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

import io.sentry.Sentry;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class DogCommand extends Command {

    public DogCommand() {
        this.category = CommandCategory.ANIMALS;
        this.name = "dog";
        this.helpFunction = (prefix, invoke) -> "Shows a dog";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final String base = "https://random.dog/";
        final GuildMessageReceivedEvent event = ctx.getEvent();
        try {
            WebUtils.ins.getText(base + "woof").async((it) -> {
                final String finalS = base + it;

                if (finalS.contains(".mp4")) {
                    sendEmbed(event, EmbedUtils.embedField("A video", "[Click for video](" + finalS + ")"));
                } else {
                    sendEmbed(event, EmbedUtils.embedImage(finalS));
                }
            });

        }
        catch (Exception e) {
            //e.printStackTrace();
            sendEmbed(event, EmbedUtils.embedMessage("**[OOPS]** Something broke, blame duncte \n(" + e.toString() + ")"));
            Sentry.capture(e);
        }

    }
}
