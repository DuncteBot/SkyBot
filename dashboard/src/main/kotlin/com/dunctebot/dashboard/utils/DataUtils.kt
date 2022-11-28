package com.dunctebot.dashboard.utils

import com.dunctebot.dashboard.duncteApis
import com.dunctebot.dashboard.jsonMapper
import com.dunctebot.dashboard.webSocket
import com.dunctebot.models.settings.GuildSetting
import com.fasterxml.jackson.databind.JsonNode
import java.util.concurrent.CompletableFuture

fun fetchGuildData(guildId: String): Pair<GuildSetting, Boolean> {
    val future = CompletableFuture<JsonNode>()
    val json = jsonMapper.createObjectNode()

    json.putArray("guild_settings")
        .add(guildId)
    json.putArray("guild_patron_status")
        .add(guildId)

    webSocket.requestData(json, future::complete)

    val result = future.get()
    var guildSetting = result["guild_settings"][guildId]

    // fallback in case the bots don't have it
    if (guildSetting == null || guildSetting.isNull) {
        guildSetting = duncteApis.fetchGuildSetting(guildId.toLong())
    }

    val settingParsed = jsonMapper.readValue(guildSetting.traverse(), GuildSetting::class.java)
    val patreonStatus = result["guild_patron_status"][guildId].asBoolean()

    return settingParsed to patreonStatus
}
