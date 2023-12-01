package com.dunctebot.dashboard.controllers.api

import com.dunctebot.dashboard.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import net.dv8tion.jda.api.Permission
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

val guildsRequests = ExpiringMap.builder()
    .expirationPolicy(ExpirationPolicy.CREATED)
    .expiration(5, TimeUnit.MINUTES)
    .build<String, List<OAuth2Guild>>()

fun fetchGuildsOfUser(ctx: Context, oAuth2Client: OAuth2Client) {
    val attributes = ctx.sessionAttributeMap()

    // We need to make sure that we are logged in and have a user id
    // If we don't have either of them we will return an error message
    if (!attributes.contains(WebServer.USER_ID) || !attributes.contains(WebServer.SESSION_ID)) {
        ctx.req().session.invalidate()

        ctx.status(HttpStatus.FORBIDDEN)
        ctx.json(
            jsonMapper.createObjectNode()
                .put("success", false)
                .put("message", "SESSION_INVALID")
                .put("code", ctx.status().code)
        )

        return
    }

    val guilds = jsonMapper.createArrayNode()

    if (!guildsRequests.contains(ctx.userId)) {
        val guildRes = oAuth2Client.getGuilds(ctx.getSession(oAuth2Client)).complete()

        guildsRequests[ctx.userId] = guildRes
    }

    // Attempt to get all guilds from the cache
    // We are using a cache here to make sure we don't rate limit our application
    val guildsRequest = guildsRequests[ctx.userId]!!

    // Only add the servers where the user has the MANAGE_SERVER
    // perms to the list
    val filteredGilds = guildsRequest.filter { it.hasPermission(Permission.MANAGE_SERVER) }

    val future = CompletableFuture<JsonNode>()
    val json = jsonMapper.createObjectNode()
    val partialRequest = json.putArray("partial_guilds")

    // add all guild ids to the map
    filteredGilds.map(OAuth2Guild::getId).forEach(partialRequest::add)

    webSocket.requestData(json, future::complete)

    val partialGuilds = future.get()["partial_guilds"]

    filteredGilds.forEach { guilds.add(guildToJson(it, partialGuilds)) }

    ctx.json(
        jsonMapper.createObjectNode()
            .put("success", true)
            .put("total", guildsRequest.size)
            .put("code", ctx.status().code)
            .set<ObjectNode>("guilds", guilds)
    )
}

private fun guildToJson(guild: OAuth2Guild, partialGuilds: JsonNode): JsonNode {
    // Get guild id or random default avatar url
    val icon = if (!guild.iconUrl.isNullOrEmpty()) {
        guild.iconUrl
    } else {
        val number = ThreadLocalRandom.current().nextInt(0, 5)
        "https://cdn.discordapp.com/embed/avatars/$number.png"
    }

    // #getId does a Long.toUnsignedString operation so we store it here to save some cycles
    val textId = guild.id
    val part = partialGuilds.find { it["id"].asText() == textId } ?: jsonMapper.createObjectNode().put("member_count", -1)

    return jsonMapper.createObjectNode()
        .put("name", guild.name)
        .put("iconId", guild.iconId)
        .put("iconUrl", icon)
        .put("owner", guild.isOwner)
        .put("members", part["member_count"].asInt())
        .put("id", textId)
}
