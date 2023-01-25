package com.dunctebot.dashboard.controllers.api

import com.dunctebot.dashboard.WebServer
import com.dunctebot.dashboard.fetchGuild
import com.dunctebot.dashboard.guildId
import com.dunctebot.dashboard.utils.fetchGuildData
import com.dunctebot.jda.json.JsonChannel
import com.dunctebot.jda.json.JsonGuild
import com.dunctebot.jda.json.JsonRole
import com.dunctebot.models.settings.GuildSetting
import com.dunctebot.models.settings.WarnAction
import io.javalin.http.Context
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.utils.TimeUtil.DISCORD_EPOCH
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

private val executor = Executors.newSingleThreadExecutor()

fun getSettings(ctx: Context) {
    val future = CompletableFuture.supplyAsync({
        val data = fetchData(ctx)

        val message: String? = ctx.sessionAttribute(WebServer.FLASH_MESSAGE)

        if (!message.isNullOrEmpty()) {
            ctx.sessionAttribute(WebServer.FLASH_MESSAGE, null)
            data["message"] = message
        } else {
            data["message"] = null
        }

        return@supplyAsync data
    }, executor)

    ctx.future { future }
}

fun postSettings(ctx: Context) {
    val future = CompletableFuture.supplyAsync({
        val (_, patron) = fetchGuildData(ctx.guildId) // string

        // What don't we need to check?
        // - filterType: this will default to good setting
        val body = ctx.bodyValidator(GuildSetting::class.java)
            .check(
                "prefix",
                { it.customPrefix.length in 1..10},
                "Prefix must be at least 1 character and at most 10 characters"
            )
            .check(
                "autorole",
                { it.autoroleRole == 0L || it.autoroleRole > DISCORD_EPOCH },
                "Autorole is not conform to discord timestamp"
            )
            .check(
                "leave_timeout",
                { it.leaveTimeout in 1..60 },
                "Leave timeout must be within range of 1 to 60 (inclusive)"
            )
            .check(
                "warn_actions",
                {
                    val size = it.warnActions.size

                    if (patron) {
                        size > 0 && size <= WarnAction.PATRON_MAX_ACTIONS
                    } else {
                        size == 1
                    }
                },
                "Non patreon supporters can only have one warn action configured"
            )
            .check(
                "warn_actions",
                {
                    it.warnActions.all { it.threshold > 0 && it.duration > 0 }
                },
                "All thresholds and durations must be greater than 0"
            )
            .check(
                "aiSensitivity",
                { it.aiSensitivity > 0.0 && it.aiSensitivity < 1.0 },
                "The ai sensitivity must be between 0.0 and 1.0"
            )
            .check(
                "logChannelId",
                { it.logChannel == 0L || it.logChannel > DISCORD_EPOCH },
                "logChannelId is not conform to discord timestamp"
            )
            .check(
                "rateLimits",
                { it.ratelimits.size == 7 && it.ratelimits.all { r -> r in 1..1000000 } },
                "Rate limits are not valid"
            )
            .get()

        return@supplyAsync body
    }, executor)

    ctx.future { future }
}

private fun fetchData(ctx: Context): MutableMap<String, Any?> {
    val map = mutableMapOf<String, Any?>()

    val guild = ctx.fetchGuild()

    if (guild != null) {
        val tcs = guild.textChannelCache
            .filter(TextChannel::canTalk)
            .map { JsonChannel(it) }
        val roles = guild.roleCache.filter {
            it.name != "@everyone" && it.name != "@here" && !it.isManaged &&
                guild.selfMember.canInteract(it)
        }.map { JsonRole(it) }

        map["channels"] = tcs
        map["roles"] = roles
        map["guild"] = JsonGuild(guild)

        val (settings, patron) = fetchGuildData(ctx.guildId) // string

        map["settings"] = settings
        map["patron"] = patron
    }

    return map
}
