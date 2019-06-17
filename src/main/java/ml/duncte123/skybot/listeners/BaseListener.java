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

package ml.duncte123.skybot.listeners;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import me.duncte123.botcommons.text.TextColor;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class BaseListener extends ListenerAdapter {

    public static boolean isUpdating = false;
    protected static boolean shuttingDown = false;
    protected static final Logger logger = LoggerFactory.getLogger(BaseListener.class);
    protected final Variables variables = Variables.getInstance();
    // A list of servers that list bots
    private static final TLongList botLists = new TLongArrayList(
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

    // Keeps track of the guilds that we are leaving as botfarms so that we don't have to check every time
    private static final Cache<Long, Character> botfarmCache = Caffeine.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    boolean isBotfarm(Guild guild) {

        if (botLists.contains(guild.getIdLong())) {
            return false;
        }

        if (botfarmCache.asMap().containsKey(guild.getIdLong())) {
            return true;
        }

        // How many members should we at least have in the server
        // before starting to conciser it as a botfarm
        final int minTotalMembers = 30;
        // What percentage of bots do we allow
        final double maxBotPercentage = 70;

        final double[] botToUserRatio = GuildUtils.getBotRatio(guild);
        final long[] counts = GuildUtils.getBotAndUserCount(guild);
        final long totalMembers = guild.getMemberCache().size();

        // if (!(botToUserRatio[1] >= maxBotPercentage && totalMembers > 30))
        logger.debug("totalMembers > minTotalMembers " + (totalMembers > minTotalMembers));
        logger.debug("botToUserRatio[1] <= maxBotPercentage " + (botToUserRatio[1] <= maxBotPercentage));

        if (!(botToUserRatio[1] >= maxBotPercentage && totalMembers > minTotalMembers)) {
            return false;
        }

        logger.debug("{}Botfarm found: {} {}% bots ({} humans / {} bots){}",
            TextColor.RED,
            guild,
            botToUserRatio[1],
            counts[0],
            counts[1],
            TextColor.RESET
        );

        botfarmCache.put(guild.getIdLong(), 'a');

        return true;
    }

}
