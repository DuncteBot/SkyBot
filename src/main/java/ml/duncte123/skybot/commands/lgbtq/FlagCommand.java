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

package ml.duncte123.skybot.commands.lgbtq;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.commands.image.ImageCommandBase;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class FlagCommand extends ImageCommandBase {

    private final List<String> flags = List.of("agender", "aromantic", "asexual", "bear", "bi", "gay",
        "genderfluid", "nonbinary", "pan", "transgender");

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();

        if (!passes(event, ctx.getArgs(), false)) {
            return;
        }

        final String flag = ctx.getArgs().get(0).toLowerCase();

        if ("list".equals(flag)) {
            sendFlagsList(event);
            return;
        }

        if (!flags.contains(flag)) {
            sendMsg(event, "I do not know what this flag is, use `" + Settings.PREFIX + getName() + " list` for a list of available flags.");
            return;
        }

        User user = ctx.getAuthor();

        if (ctx.getArgs().size() > 1) {
            final String search = String.join(" ", args.subList(1, args.size()));
            final List<User> foundUsers = FinderUtil.findUsers(search, ctx.getJDA());

            if (!foundUsers.isEmpty()) {
                user = foundUsers.get(0);
            }
        }

        final String imageUrl = user.getEffectiveAvatarUrl().replace("gif", "png") + "?size=512";
        final byte[] image = ctx.getApis().getFlag(flag, imageUrl);

        handleBasicImage(event, image);

    }

    private void sendFlagsList(GuildMessageReceivedEvent event) {
        final String message = "Current list of available flags, if a flag is missing contact duncte123#1245 to have it added.\n" +
            "**Agender** => Agender pride flag\n" +
            "**Aromantic** => Aromantic pride flag\n" +
            "**Asexual** => Asexuality pride flag\n" +
            "**Bear** => Bear Brotherhood pride flag\n" +
            "**Bi** => Bisexuality pride flag\n" +
            "**Gay** => Gay pride flag\n" +
            "**Genderfluid** => Genderfluidity pride flag\n" +
            "**Nonbinary** => Nonbinary pride flag\n" +
            "**Pan** => Pansexuality pride flag\n" +
            "**Transgender** => Transgender pride flag";

        sendMsg(event, message);
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.LGBTQ;
    }

    @Override
    public String getName() {
        return "flag";
    }

    @Override
    public String help() {
        return "Overlay your profile picture with a pride flag.\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <flag/list> [username]`";
    }
}
