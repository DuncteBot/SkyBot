package com.dunctebot.dashboard.websocket.handlers

import com.dunctebot.dashboard.controllers.GuildController
import com.dunctebot.dashboard.websocket.handlers.base.SocketHandler
import com.fasterxml.jackson.databind.JsonNode

class RolesHashHandler : SocketHandler() {
    override fun handleInternally(data: JsonNode?) {
        val hash = data!!["hash"].asText()
        val guildId = data["guild_id"].asLong()

        logger.debug("Adding hash for $guildId: $hash")

        GuildController.guildHashes.put(hash, guildId)
    }
}
