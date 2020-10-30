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

package ml.duncte123.skybot.listeners;

import ml.duncte123.skybot.Variables;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseListener implements EventListener {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseListener.class);
    // TODO: use a scheduler instead
    protected final ExecutorService handlerThread = Executors.newCachedThreadPool((r) -> {
        final Thread thread = new Thread(r, "Listener-handle-thread");
        thread.setDaemon(true);
        return thread;
    });
    protected final Variables variables;
    // A list of servers that list bots
    /*private static final TLongList botLists = new TLongArrayList(
        new long[]{
            110373943822540800L, // Dbots
            264445053596991498L, // Dbl
            374071874222686211L, // Bots for discord
            112319935652298752L, // Carbon
            439866052684283905L, // Discord Boats
            387812458661937152L, // Botlist.space
            454933217666007052L, // Divine Discord Bot List
            568567800910839811L, // discordextremelist.xyz
        }
    );*/

    // Keeps track of the guilds that we are leaving as botfarms so that we don't have to check every time
    /*private static final Cache<Long, Character> botfarmCache = Caffeine.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();*/

    /* package */ BaseListener(Variables variables) {
        this.variables = variables;
    }

    /**
     * Checks if a guild is a bot-farm, we will ignore botfarms
     *
     * @param guild the guild to check
     * @return true if we consider this guild a botfarm
     */
    /* package */ boolean isBotfarm(Guild guild) {
        // TODO: Fix this check
        return false;

        /*if (botLists.contains(guild.getIdLong())) {
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
        final long totalMembers = guild.getMemberCount();

        // if (!(botToUserRatio[1] >= maxBotPercentage && totalMembers > 30))
        LOGGER.debug("totalMembers > minTotalMembers " + (totalMembers > minTotalMembers));
        LOGGER.debug("botToUserRatio[1] <= maxBotPercentage " + (botToUserRatio[1] <= maxBotPercentage));

        if (!(botToUserRatio[1] >= maxBotPercentage && totalMembers > minTotalMembers)) {
            return false;
        }

        LOGGER.debug("{}Botfarm found: {} {}% bots ({} humans / {} bots){}",
            TextColor.RED,
            guild,
            botToUserRatio[1],
            counts[0],
            counts[1],
            TextColor.RESET
        );

        botfarmCache.put(guild.getIdLong(), 'a');

        return true;*/
    }

}
