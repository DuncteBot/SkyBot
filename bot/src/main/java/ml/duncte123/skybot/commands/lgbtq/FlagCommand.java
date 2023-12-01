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

package ml.duncte123.skybot.commands.lgbtq;

import com.fasterxml.jackson.databind.JsonNode;
import kotlin.Pair;
import ml.duncte123.skybot.commands.image.ImageCommandBase;
import ml.duncte123.skybot.extensions.UserKt;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.FinderUtils;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class FlagCommand extends ImageCommandBase {

    private final List<String> flags = List.of("agender", "aromantic", "asexual", "bear", "bi", "gay",
        "genderfluid", "nonbinary", "pan", "transgender", "demigirl", "lesbian");

    public FlagCommand() {
        this.category = CommandCategory.LGBTQ;
        this.name = "flag";
        this.help = "Overlay your profile picture with a pride flag";
        this.usage = "<flag/list> [@user]";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (!passes(ctx, false)) {
            return;
        }

        final List<String> args = ctx.getArgs();
        final String flag = args.get(0).toLowerCase();

        if ("list".equalsIgnoreCase(flag)) {
            sendMsg(ctx, "A list of flags can be found at https://duncte.bot/flags");

            return;
        }

        if (!flags.contains(flag)) {
            sendMsg(ctx, "I do not know what this flag is, visit <https://duncte.bot/flags> for a list of available flags.");

            return;
        }

        User user = ctx.getAuthor();

        if (args.size() > 1) {
            final String search = String.join(" ", args.subList(1, args.size()));
            final List<User> foundUsers = FinderUtils.searchUsers(search, ctx);

            if (!foundUsers.isEmpty()) {
                user = foundUsers.get(0);
            }
        }

        final String imageUrl = UserKt.getStaticAvatarUrl(user) + "?size=512";
        final Pair<byte[], JsonNode> image = ctx.getApis().getFlag(flag, imageUrl);

        handleBasicImage(ctx, image);

    }

    // Has to be overwritten here due to override in ImageCommandBase
    @Nonnull
    @Override
    public CommandCategory getCategory() {
        return CommandCategory.LGBTQ;
    }
}
