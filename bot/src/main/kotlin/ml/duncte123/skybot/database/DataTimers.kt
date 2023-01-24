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

package ml.duncte123.skybot.database

import io.sentry.Sentry
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.ModerationUtils.handleUnban
import ml.duncte123.skybot.utils.ModerationUtils.handleUnmute
import org.slf4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object DataTimers {
    private val pool = Executors.newScheduledThreadPool(2)

    @JvmStatic
    fun startUnbanTimer(variables: Variables, logger: Logger) {
        logger.info("Starting unban timer")

        pool.scheduleAtFixedRate(
            {
                checkUnbansAndUnmutes(variables)
            },
            0L,
            1L,
            TimeUnit.MINUTES
        )
    }

    @JvmStatic
    fun startReminderTimer(variables: Variables, logger: Logger) {
        logger.info("Starting reminder timer")

        pool.scheduleAtFixedRate(
            {
                val db = variables.database

                db.getExpiredReminders().thenAccept {
                    if (it.isNotEmpty()) {
                        AirUtils.handleExpiredReminders(it, db)
                    }
                }
            },
            0L,
            30L,
            TimeUnit.SECONDS
        )
    }

    @JvmStatic
    private fun checkUnbansAndUnmutes(variables: Variables) {
        // we're working in a thread here, fuck async :)
        try {
            val db = variables.database
            val (bans, mutes) = db.getExpiredBansAndMutes().get()

            handleUnban(bans, db, variables)
            handleUnmute(mutes, db, variables)
        } catch (e: Exception) {
            Sentry.captureException(e)
            e.printStackTrace()
        }
    }
}
