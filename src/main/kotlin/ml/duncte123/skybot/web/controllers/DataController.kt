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

package ml.duncte123.skybot.web.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.api.AllPatronsData
import ml.duncte123.skybot.objects.api.Ban
import ml.duncte123.skybot.objects.api.Mute
import ml.duncte123.skybot.objects.api.Reminder
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.CommandUtils.addPatronsFromData
import ml.duncte123.skybot.utils.CommandUtils.removePatronsFromData
import ml.duncte123.skybot.utils.ModerationUtils
import net.dv8tion.jda.api.sharding.ShardManager
import spark.Request
import spark.Spark

object DataController {

    fun updateData(request: Request, jackson: ObjectMapper, shardManager: ShardManager, variables: Variables): Any {
        // Logging in with the bots own token
        if (!request.headers().contains("Authorization") || request.headers("Authorization") != shardManager.getShardById(0)?.token) {
            Spark.halt(401)
        }

        val updateData = jackson.readTree(request.bodyAsBytes())

        println(updateData)

        if (updateData.has("patrons")) {
            val patronData = updateData["patrons"]

            if (patronData.has("add")) {
                val addedPatrons = jackson.readValue(patronData["add"].traverse(), AllPatronsData::class.java)

                addPatronsFromData(addedPatrons)
            }

            if (patronData.has("remove")) {
                val removedPatrons = jackson.readValue(patronData["remove"].traverse(), AllPatronsData::class.java)

                removePatronsFromData(removedPatrons)
            }
        }

        if (updateData.has("unbans")) {
            val expiredBans = updateData["unbans"]
            val bans: List<Ban> = jackson.readValue(expiredBans.traverse(), object : TypeReference<List<Ban>>() {})

            ModerationUtils.handleUnban(bans, variables.databaseAdapter, variables)
        }

        if (updateData.has("unmutes")) {
            val expiredMutes = updateData["unmutes"]
            val mutes: List<Mute> = jackson.readValue(expiredMutes.traverse(), object : TypeReference<List<Mute>>() {})

            ModerationUtils.handleUnmute(mutes, variables.databaseAdapter, variables)
        }

        if (updateData.has("reminders")) {
            val expiredReminders = updateData["reminders"]
            val reminders: List<Reminder> = jackson.readValue(expiredReminders.traverse(), object : TypeReference<List<Reminder>>() {})

            if (reminders.isNotEmpty()) {
                AirUtils.handleExpiredReminders(reminders, variables.databaseAdapter, variables.prettyTime)
            }
        }

        return jackson.createObjectNode().put("success", true)
    }

}
