package com.dunctebot.dashboard.controllers

import com.dunctebot.dashboard.*
import com.dunctebot.dashboard.WebServer.Companion.OLD_PAGE
import com.dunctebot.dashboard.WebServer.Companion.SESSION_ID
import com.dunctebot.dashboard.WebServer.Companion.USER_ID
import io.javalin.http.Context
import io.javalin.http.RedirectResponse
import net.dv8tion.jda.api.Permission

object DashboardController {
    fun before(ctx: Context) {
        if (ctx.sessionAttribute<String?>(USER_ID) == null || ctx.sessionAttribute<String?>(SESSION_ID) == null) {
            ctx.sessionAttribute(OLD_PAGE, ctx.path())
            ctx.redirect("/")

            throw RedirectResponse()
        }

        val guild = ctx.fetchGuild() ?: throw haltDiscordError(ctx, DiscordError.NO_GUILD, ctx.guildId)

        val member = try {
            restJDA.retrieveMemberById(guild, ctx.userId).complete()
        } catch (e: Exception) {
            e.printStackTrace()
            throw haltDiscordError(ctx, DiscordError.WAT)
        }

        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            throw haltDiscordError(ctx, DiscordError.NO_PERMS)
        }
    }
}
