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

package me.duncte123.skybot.web.handlers

import com.dunctebot.models.settings.GuildSetting
import com.fasterxml.jackson.databind.JsonNode
import me.duncte123.skybot.EventManager
import me.duncte123.skybot.SkyBot
import me.duncte123.skybot.Variables
import me.duncte123.skybot.extensions.isUnavailable
import me.duncte123.skybot.listeners.InviteTrackingListener
import me.duncte123.skybot.utils.CommandUtils
import me.duncte123.skybot.web.WebSocketClient
import me.duncte123.skybot.websocket.SocketHandler
import net.dv8tion.jda.api.entities.Guild

class GuildSettingsHandler(private val variables: Variables, client: WebSocketClient) : SocketHandler(client) {
    override fun handleInternally(data: JsonNode) {
        if (data.has("remove")) {
            removeGuildSettings(data["remove"])
        }

        if (data.has("update")) {
            updateGuildSettings(data["update"])
        }
    }

    private fun updateGuildSettings(guildSettings: JsonNode) {
        val shardManager = SkyBot.getInstance().shardManager
        val settings = variables.guildSettingsCache

        guildSettings.forEach {
            val setting = variables.jackson.readValue(it.traverse(), GuildSetting::class.java)
            val guild = shardManager.getGuildById(setting.guildId)

            // only update the setting if we have the guild in cache
            if (guild != null) {
                // fetch the old setting before updating it
                val oldSetting = settings.getIfPresent(setting.guildId)

                settings[setting.guildId] = setting

                // if the guild is there we attempt cache the invites
                if (!shardManager.isUnavailable(setting.guildId)) {
                    val tracker = guild.globalInviteTracker

                    // setting was turned on
                    if (oldSetting?.isFilterInvites == false && setting.isFilterInvites) {
                        if (CommandUtils.isGuildPatron(guild)) {
                            tracker.attemptInviteCaching(guild)
                        }
                        // setting was turned off
                    } else if (oldSetting?.isFilterInvites == true && !setting.isFilterInvites) {
                        tracker.clearInvites(setting.guildId)
                    }
                }

                variables.database.updateGuildSetting(setting)
            }
        }
    }

    private fun removeGuildSettings(guildsIds: JsonNode) {
        guildsIds.forEach {
            val longId = it.asLong()

            variables.guildSettingsCache.remove(longId)
            variables.vcAutoRoleCache.remove(longId)
        }
    }

    private val Guild.globalInviteTracker: InviteTrackingListener
        get() = (this.jda.eventManager as EventManager).inviteTracker
}
