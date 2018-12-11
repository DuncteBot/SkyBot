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

package ml.duncte123.skybot.listeners;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import me.duncte123.botcommons.text.TextColor;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.utils.GuildUtils;
import ml.duncte123.skybot.utils.SpamFilter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class BaseListener extends ListenerAdapter {

    public static boolean isUpdating = false;
    protected final Logger logger;
    protected final SpamFilter spamFilter;

    protected static boolean shuttingDown = false;

    protected final Variables variables;

    public BaseListener(Variables variables) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.variables = variables;
        this.spamFilter = new SpamFilter(variables);
    }

    // A list of servers that list bots
    private final TLongList botLists = new TLongArrayList(
        new long[]{
            110373943822540800L, // Dbots
            264445053596991498L, // Dbl
            374071874222686211L, // Bots for discord
            112319935652298752L, // Carbon
            439866052684283905L, // Discord Boats
            387812458661937152L, // Botlist.space
            483344253963993113L, // AutomaCord
            454933217666007052L, // Divine Discord Bot List
            446682534135201793L, // Discords best bots
            477792727577395210L, // discordbotlist.xyz
            475571221946171393L, // bots.discordlist.app
        }
    );

    protected boolean isBotfarm(Guild guild) {

        if (botLists.contains(guild.getIdLong())) {
            return false;
        }

        // How many members should we at least have in the server
        // before starting to conciser it as a botfarm
        int minTotalMembers = 30;
        // What percentage of bots do we allow
        double maxBotPercentage = 70;

        double[] botToUserRatio = GuildUtils.getBotRatio(guild);
        long[] counts = GuildUtils.getBotAndUserCount(guild);
        long totalMembers = guild.getMemberCache().size();

        // if (!(botToUserRatio[1] >= maxBotPercentage && totalMembers > 30))
        logger.debug("totalMembers > minTotalMembers " + (totalMembers > minTotalMembers));
        logger.debug("botToUserRatio[1] <= maxBotPercentage " + (botToUserRatio[1] <= maxBotPercentage));
        if (!(botToUserRatio[1] >= maxBotPercentage && totalMembers > minTotalMembers)) {
            return false;
        }

        sendMsg(GuildUtils.getPublicChannel(guild),
            String.format("Hello %s, this server is now blacklisted as botfarm and the bot will leave the guild (%s humans / %s bots).",
                guild.getOwner().getAsMention(),
                counts[0],
                counts[1]
            ),
            message -> guild.leave().queue(),
            er -> guild.leave().queue()
        );

        logger.info("{}Botfarm found: {} {}% bots ({} humans / {} bots){}",
            TextColor.RED,
            guild,
            botToUserRatio[1],
            counts[0],
            counts[1],
            TextColor.RESET
        );

        return true;
    }

}
