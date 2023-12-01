package com.dunctebot.dashboard.controllers

import com.dunctebot.dashboard.WebServer.Companion.HOMEPAGE
import com.dunctebot.dashboard.WebServer.Companion.OLD_PAGE
import com.dunctebot.dashboard.WebServer.Companion.SESSION_ID
import com.dunctebot.dashboard.WebServer.Companion.USER_ID
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.OAuth2Client.DISCORD_REST_VERSION
import com.jagrosh.jdautilities.oauth2.Scope
import com.jagrosh.jdautilities.oauth2.exceptions.InvalidStateException
import io.javalin.http.Context
import org.slf4j.LoggerFactory

object RootController {
    private val logger = LoggerFactory.getLogger(RootController::class.java)

    fun beforeRoot(ctx: Context, oAuth2Client: OAuth2Client) {
        if (ctx.sessionAttribute<String?>(SESSION_ID) == null) {
            var url = oAuth2Client.generateAuthorizationURL(
                // Is this safe? Yes, you could change the query param anyway and oauth prevents that
                "${ctx.scheme()}://${ctx.host()}/callback",
                Scope.IDENTIFY, Scope.GUILDS
            )

            val debug = ctx.queryParam("debug")

            if (!debug.isNullOrBlank()) {
                url = url.replace("discord.com", "$debug.discord.com")
                    .replace("/api/v$DISCORD_REST_VERSION", "")
            }

            ctx.sessionAttribute(SESSION_ID, "session_${System.currentTimeMillis()}")

            ctx.redirect("$url&prompt=none")
//            ctx.redirect(url)
        }
    }

    fun callback(ctx: Context, oAuth2Client: OAuth2Client) {
        val query = ctx.queryParamMap()

        // If we don't have a code from discord
        // and we don't have a state we will return the user to the homepage
        if (!query.containsKey("code") || !query.containsKey("state")) {
            return ctx.redirect(HOMEPAGE)
        }

        // Get the session id for the user
        val sesId = ctx.sessionAttribute<String?>(SESSION_ID) ?: return ctx.redirect(HOMEPAGE)

        // If the session is missing we will return the user to the homepage

        try {
            // Start a session to obtain the oauth2 access token
            val oauthses = oAuth2Client.startSession(
                ctx.queryParam("code"),
                ctx.queryParam("state"),
                sesId,
                Scope.IDENTIFY, Scope.GUILDS
            ).complete()

            // Fetch the user from discord
            val userId = oAuth2Client.getUser(oauthses).complete().id

            // Store the user id in the session
            ctx.sessionAttribute(USER_ID, userId)

            // If we have a previous page we will return the user there
            if (ctx.sessionAttribute<String?>(OLD_PAGE) != null) {
                return ctx.redirect(ctx.sessionAttribute<String>(OLD_PAGE)!!)
            }

            // Otherwise the user will be send to the dashboard homepage
            ctx.redirect("/")
        } catch (stateEx: InvalidStateException) {
            "<h1>${stateEx.message}</h1><br /><a href=\"${HOMEPAGE}\">Click here to go back home</a>"
        } catch (e: Exception) {
            logger.error("Failed to log user in with discord", e)

            // If we fail to log in we will return the user back home
            return ctx.redirect(HOMEPAGE)
        }
    }
}
