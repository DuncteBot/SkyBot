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

package me.duncte123.skybot.commands.lgbtq;

import me.duncte123.skybot.extensions.StringKt;
import me.duncte123.skybot.objects.command.Command;
import me.duncte123.skybot.objects.command.CommandCategory;
import me.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.skybot.objects.command.Flag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class SetPronounsCommand extends Command {

    /*
        %1$s they
        %2$s them
        %3$s their
        %4$s theirs
        %5$s is/are
     */
    private final String[] messages = {
        "%6$s went into the kitchen.\n" +
            "**%1$s** brought **%3$s** groceries with **%2$s**.\n" +
            "At least I think the groceries were **%4$s**.\n" +
            "**%1$s %5$s** making dinner tonight, after all.",

        "**%1$s %5$s** a writer and wrote that book **%2$sself**. \n" +
            "Those ideas are **%4$s**. \n" +
            "I like both **%2$s** and **%3$s** ideas.",

        "**%1$s** went to the park.\n" +
            "I went with **%2$s**.\n" +
            "**%1$s** brought **%3$s** frisbee.\n" +
            "**%1$s** threw the frisbee to **%2$sself**."
    };

    public SetPronounsCommand() {
        this.category = CommandCategory.LGBTQ;
        this.name = "setpronouns";
        this.help = "Set your pronouns for people can check them with `{prefix}pronounscheck`";
        this.usage = "<pronouns> [--plural]";
        this.extraInfo = "Examples of pronouns are:\n" +
                "```they/them/their/theirs\n" +
                "she/her/her/hers\n" +
                "he/him/his/his\n" +
                "ze/zir/zir/zirs\n" +
                "xe/xir/xir/xirs```";
        this.flags = new Flag[]{
            new Flag(
                'p',
                "plural",
                "Marks your pronouns as being plural (is vs are)"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (ctx.getArgs().isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final String pronouns = StringKt.stripFlags(ctx.getArgsRaw(), this);
        final String[] pronounsSplit = pronouns.split("/");

        if (pronounsSplit.length != 4) {
            sendMsg(ctx, "Incorrect format, check `" + ctx.getPrefix() + "help " + getName() + '`');
            return;
        }

        if (pronouns.length() > 30) {
            sendMsg(ctx, "Maximum length for pronouns is 30 characters, " +
                "please contact duncte123#1245 to have this changed");
            return;
        }

        final boolean singular = !ctx.getParsedFlags(this).containsKey("p");
        final String format = this.messages[ctx.getRandom().nextInt(messages.length)];
        final List<String> items = new ArrayList<>(Arrays.asList(pronounsSplit));
        items.add(singular ? "is" : "are");
        items.add(ctx.getAuthor().getName());

        final String message = String.format("Your pronouns have been set to **%s**\n", pronouns) +
            "Here is a preview:\n" +
            String.format(format, items.toArray());

        sendMsg(ctx, message);

        ctx.getApis().setPronouns(ctx.getAuthor().getIdLong(), pronouns, singular);
    }
}
