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
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import javax.annotation.Nonnull;
import org.json.JSONObject;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.AirUtils.getMentionedUser;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class PronounsCheckCommand extends Command {

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final User target = getMentionedUser(ctx);
        final long userId = target.getIdLong();
        final JSONObject json = ctx.getApis().getPronouns(userId);
        final boolean isSelf = userId == ctx.getAuthor().getIdLong();

        if (json == null) {
            sendMsg(event, (isSelf ? "You do" : target.getName() + " does") + " not have any pronouns set");
            return;
        }

        final String pronouns = json.getString("pronouns");
        final String singular = json.getBoolean("singular") ? "Singular" : "Plural";
        final String userName = isSelf ? "Your" : target.getName() + "'s";

        final String format = "%s current pronouns are:%n**%s** (%s)";

        sendMsg(event, String.format(format, userName, pronouns, singular));
    }

    @Override
    public String getName() {
        return "pronounscheck";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"pronouns"};
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.LGBTQ;
    }

    @Override
    public String help() {
        return "Check someones pronouns.\n" +
            "Usage: `" + Settings.PREFIX + getName() + " [user]`\n" +
            "Pronouns can be set via `" + Settings.PREFIX + "setpronouns`";
    }
}
