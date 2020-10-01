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

package ml.duncte123.skybot.web.handlers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.api.AllPatronsData
import ml.duncte123.skybot.objects.api.Ban
import ml.duncte123.skybot.objects.api.Mute
import ml.duncte123.skybot.objects.api.Reminder
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.CommandUtils
import ml.duncte123.skybot.utils.ModerationUtils
import ml.duncte123.skybot.web.WebSocketClient
import ml.duncte123.skybot.websocket.SocketHandler
import net.dv8tion.jda.api.entities.Role

class DataUpdateHandler(private val variables: Variables, client: WebSocketClient) : SocketHandler(client) {
    private val jackson = variables.jackson

    override fun handleInternally(data: JsonNode) {
        GlobalScope.launch(context = Dispatchers.IO) {
            if (data.has("new_one_guild")) {
                handleNewOneGuild(data["new_one_guild"])
            }

            if (data.has("patrons")) {
                handlePatrons(data["patrons"])
            }

            if (data.has("unbans")) {
                handleUnbans(data["unbans"])
            }

            if (data.has("unmutes")) {
                handleUnmutes(data["unmutes"])
            }

            // Uses complete, must be handled last
            if (data.has("reminders")) {
                handleReminders(data["reminders"])
            }
        }
    }

    private fun handleNewOneGuild(data: JsonNode) {
        val userId = data["user_id"].asLong()
        val guildId = data["guild_id"].asLong()

        if (CommandUtils.oneGuildPatrons.containsKey(userId)) {
            return
        }

        variables.databaseAdapter.addOneGuildPatrons(userId, guildId) { _, _ ->
            val instance = SkyBot.getInstance()
            val dbGuild = instance.shardManager.getGuildById(Settings.SUPPORT_GUILD_ID) ?: return@addOneGuildPatrons
            val newPatron = dbGuild.getMemberById(userId) ?: return@addOneGuildPatrons

            val hasRole = newPatron.roles
                .map(Role::getIdLong)
                .any { it == Settings.ONE_GUILD_PATRONS_ROLE }

            if (hasRole) {
                CommandUtils.oneGuildPatrons.put(userId, guildId)
            }
        }
    }

    private fun handlePatrons(patrons: JsonNode) {
        if (patrons.has("add")) {
            val addedPatrons = jackson.readValue(patrons["add"].traverse(), AllPatronsData::class.java)

            CommandUtils.addPatronsFromData(addedPatrons)
        }

        if (patrons.has("remove")) {
            val removedPatrons = jackson.readValue(patrons["remove"].traverse(), AllPatronsData::class.java)

            CommandUtils.removePatronsFromData(removedPatrons)
        }
    }

    private fun handleUnbans(unbans: JsonNode) {
        val bans: List<Ban> = jackson.readValue(unbans.traverse(), object : TypeReference<List<Ban>>() {})

        ModerationUtils.handleUnban(bans, variables.databaseAdapter, variables)
    }

    private fun handleUnmutes(unmutes: JsonNode) {
        val mutes: List<Mute> = jackson.readValue(unmutes.traverse(), object : TypeReference<List<Mute>>() {})

        ModerationUtils.handleUnmute(mutes, variables.databaseAdapter, variables)
    }

    private fun handleReminders(reminders: JsonNode) {
        val parsedReminders: List<Reminder> = jackson.readValue(reminders.traverse(), object : TypeReference<List<Reminder>>() {})

        // Uses complete, must be handled last
        if (parsedReminders.isNotEmpty()) {
            AirUtils.handleExpiredReminders(parsedReminders, variables.databaseAdapter, variables.prettyTime)
        }
    }
}
