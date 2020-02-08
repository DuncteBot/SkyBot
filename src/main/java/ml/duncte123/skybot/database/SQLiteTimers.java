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

package ml.duncte123.skybot.database;

import me.duncte123.botcommons.text.TextColor;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.objects.api.Ban;
import ml.duncte123.skybot.objects.api.Mute;
import ml.duncte123.skybot.utils.AirUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ml.duncte123.skybot.utils.ModerationUtils.handleUnban;
import static ml.duncte123.skybot.utils.ModerationUtils.handleUnmute;

@SuppressWarnings("unused")
public class SQLiteTimers {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteTimers.class);
    private static final ScheduledExecutorService systemPool = Executors.newScheduledThreadPool(4,
        (r) -> {
            final Thread thread = new Thread(r, "Sql-timer-thread");
            thread.setDaemon(true);
            return thread;
        });

    public static void startUnbanTimer(Variables variables) {
        logger.info("Starting the unban + unmute timer! {}(SQLITE){}", TextColor.RED, TextColor.RESET);
        //Register the timer for the auto unbans
        systemPool.scheduleAtFixedRate(() -> checkUnbansAndUnmutes(variables), 0, 2, TimeUnit.MINUTES);
    }

    public static void startReminderTimer(Variables variables) {
        logger.info("Starting reminder checker! {}(SQLITE){}", TextColor.RED, TextColor.RESET);
        systemPool.scheduleAtFixedRate(
            () -> variables.getDatabaseAdapter().getExpiredReminders((reminders) -> {
                AirUtils.handleExpiredReminders(reminders, variables.getDatabaseAdapter(), variables.getPrettyTime());
                return null;
            }), 2, 2, TimeUnit.MINUTES);
    }

    private static void checkUnbansAndUnmutes(Variables variables) {
        variables.getDatabaseAdapter().getExpiredBansAndMutes(
            (bansAndMutes) -> {
                final DatabaseAdapter adapter = variables.getDatabaseAdapter();
                final List<Ban> bans = bansAndMutes.getFirst();
                final List<Mute> mutes = bansAndMutes.getSecond();

                handleUnban(bans, adapter, variables);
                handleUnmute(mutes, adapter, variables);

                return null;
            }
        );
    }
}
