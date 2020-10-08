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

package ml.duncte123.skybot.commands.uncategorized;

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class VoteCommand extends Command {
    public VoteCommand() {
        this.name = "vote";
        this.aliases = new String[]{
            "upvote",
        };
        this.help = "Gives some links where you can upvote the bot";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        sendEmbed(ctx, EmbedUtils.embedMessage(
                "Votes are always welcome :D\n" +
                    "You can use the following links:\n" +
                    "BFD - " + link("https://botsfordiscord.com/bot/210363111729790977/vote") +
                    "DBoats - " + link("https://discord.boats/bot/210363111729790977/vote") +
                    "botlist.space - " + link("https://botlist.space/bot/210363111729790977/upvote") +
                    "Divine - " + link("https://divinediscordbots.com/bot/210363111729790977/vote") +
                    "DiscordsBestBots - " + link("https://discordsbestbots.xyz/bots/210363111729790977") +
                    "DBL - " + link("https://discordbots.org/bot/210363111729790977/vote")
        ));
    }

    private String link(String input) {
        return '[' + input + "](" + input + ")\n";
    }
}
