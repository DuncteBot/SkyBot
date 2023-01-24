package com.dunctebot.dashboard.controllers

import com.dunctebot.dashboard.*
import com.dunctebot.dashboard.WebServer.Companion.GUILD_ID
import com.dunctebot.dashboard.bodies.PatronBody
import com.dunctebot.jda.json.JsonRole
import com.fasterxml.jackson.annotation.JsonInclude
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.http.NotFoundResponse
import io.javalin.plugin.rendering.vue.VueComponent
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

object GuildController {
    // some hash -> "$userId-$guildId"
    val securityKeys = mutableMapOf<String, String>()
    val guildHashes = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(2, TimeUnit.HOURS)
        .build<String, Long>()
    val guildRoleCache = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(1, TimeUnit.HOURS)
        .build<Long, CustomRoleList>()

    fun handleOneGuildRegister(ctx: Context) {
        val body = ctx.bodyValidator<PatronBody>()
            .check(
                "token",
                { !it.token.isNullOrBlank() && securityKeys.containsKey(it.token) },
                "Submitted token is not valid."
            )
            .check(
                "userId",
                { it.userId.toSafeLong() > 0L },
                "User id is not valid."
            )
            .check(
                "guildId",
                { it.guildId.toSafeLong() > 0L },
                "Guild id is not valid."
            )
            .get()

        val token = body.token!!
        val userId = body.userId!!
        val guildId = body.guildId!!
        val theFormat = "$userId-$guildId"

        if (securityKeys[token] != theFormat) {
            ctx.status(HttpCode.BAD_REQUEST)

            val obj = jsonMapper.createObjectNode()
                .put("message", "Submitted token is not valid.")

            ctx.json(obj)
            return
        }

        // remove the token as it is used
        securityKeys.remove(token)

        if (duncteApis.isOneGuildPatron(userId)) {
            ctx.status(HttpCode.BAD_REQUEST)

            val obj = jsonMapper.createObjectNode()
                .put("message", "You already registered yourself for these perks, please contact a bot admin to have it changed.")

            ctx.json(obj)
            return
        }

        val sendData = jsonMapper.createObjectNode()
            .put("t", "DATA_UPDATE")

        sendData.putObject("d")
            .putObject("new_one_guild")
            .put("user_id", userId)
            .put("guild_id", guildId)

        webSocket.broadcast(sendData)

        ctx.status(HttpCode.OK)

        val obj = jsonMapper.createObjectNode()
            .put("message", "Server successfully registered.")
            .put("code", 200)

        ctx.json(obj)
    }

    fun showGuildRoles(ctx: Context) {
        val hash = ctx.pathParam("hash")
        val guildId = guildHashes[hash] ?: throw NotFoundResponse()
        val guild = try {
            // TODO: do we want to do this?
            // Maybe only cache for a short time as it will get outdated data
            restJDA.jda.getGuildById(guildId) ?: restJDA.retrieveGuildById(guildId.toString()).complete()
        } catch (e: ErrorResponseException) {
            e.printStackTrace()
            throw NotFoundResponse()
        }

        if (!guildRoleCache.containsKey(guild.idLong)) {
            val members = restJDA.retrieveAllMembers(guild).stream().toList()

            guildRoleCache[guild.idLong] = CustomRoleList(guild.name, guild.roles.map { CustomRole(it, members) })
        }

        // terrible way of doing this, but it works well enough
        // we're sending the decoded guild id into the state of javalin
        VueComponent("roles", mapOf("guildId" to guild.id)).handle(ctx)
    }

    fun guildRolesApiHandler(ctx: Context) {
        val guildId = ctx.pathParam(GUILD_ID).toLong()
        // will ensure that the cache is validated and we can't randomly request guilds
        val cache = guildRoleCache[guildId] ?: throw NotFoundResponse()

        ctx.json(cache)
    }

    @Suppress("unused")
    class CustomRoleList(val guildName: String, val roles: List<CustomRole>)

    class CustomRole(role: Role, allMembers: List<Member>): JsonRole(role) {
        @JsonInclude
        @Suppress("unused")
        val memberCount = allMembers.filter {
            role.name == "@everyone" || it.roles.contains(role)
        }.size

        val colorRaw = role.colorRaw.toString()
    }
}
