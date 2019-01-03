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

package ml.duncte123.skybot.commands.lgbtq;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetPronounsCommand extends Command {

    private static final String PRONOUNS_REGEX = "([a-z]+\\/[a-z]+\\/[a-z]+\\/[a-z]+)(?:.*)";

    private final String[] messages = {
        "%6$s went into the kitchen.\n" +
            "**%1$s** brought **%3$s** groceries with **%2$s**.\n" +
            "At least I think the groceries were **%4$s**.\n" +
            "**%1$s %5$s** making dinner tonight, after all.",

        "**%1$s %5$s** a writer and wrote that book **%2$sself**. \n" +
            "Those ideas are **%4$s**. \n" +
            "I like both **%2$s** and **%3$s** ideas."
    };

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (ctx.getArgs().isEmpty()) {
            sendMsg(event, "Correct usage: `" + Settings.PREFIX + getName() + " <pronouns> [--plural]`");
            return;
        }

        final Pattern pattern = Pattern.compile(PRONOUNS_REGEX);
        final Matcher matcher = pattern.matcher(ctx.getArgsRaw());

        if (!matcher.matches()) {
            sendMsg(event, "Incorrect format, check `" + Settings.PREFIX + "help " + getName() + '`');
            return;
        }

        final String pronouns = matcher.group(1);

        if (pronouns.length() > 30) {
            sendMsg(event, "Maximum length for pronouns is 30 characters, " +
                "please contact duncte123#1245 to have this changed");
            return;
        }

        final String[] pronounsSplit = pronouns.split("/");
        final boolean singular = !ctx.getArgsRaw().contains("--plural");
        final String format = this.messages[ctx.getRandom().nextInt(messages.length)];
        final List<String> items = new ArrayList<>(Arrays.asList(pronounsSplit));
        items.add(singular ? "is" : "are");
        items.add(ctx.getAuthor().getName());

        final String message = String.format("Your pronouns have been set to **%s**\n", pronouns) +
            "Here is a preview:\n" +
            String.format(format, items.toArray());

        sendMsg(event, message);

        ctx.getApis().setPronouns(ctx.getAuthor().getIdLong(), pronouns, singular);
    }

    @Override
    public String getName() {
        return "setpronouns";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.LGBTQ;
    }

    @Override
    public String help() {
        return "Set your pronouns to people can check them with `" + Settings.PREFIX + "pronounscheck`\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <pronouns> [--plural]`\n" +
            "Examples of pronouns are:\n" +
            "```they/them/their/theirs\n" +
            "she/her/her/hers\n" +
            "he/him/his/his\n" +
            "ze/zir/zir/zirs\n" +
            "xe/xir/xir/xirs```\n" +
            "Use the `--plural` flag to mark your pronouns as plural (is vs are)";
    }
}
