package com.dunctebot.dashboard

import com.dunctebot.dashboard.WebServer.Companion.GUILD_ID
import com.dunctebot.dashboard.WebServer.Companion.SESSION_ID
import com.dunctebot.dashboard.WebServer.Companion.USER_ID
import com.fasterxml.jackson.databind.JsonNode
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.session.Session
import io.javalin.http.ContentType
import io.javalin.http.Context
import io.javalin.http.ForbiddenResponse
import io.javalin.http.UnauthorizedResponse
import io.javalin.vue.VueComponent
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.internal.utils.IOUtil
import okhttp3.FormBody

fun Context.plainText(): Context = this.contentType("text/plain")

val Context.jsonBody: JsonNode
    get() = jsonMapper.readTree(this.bodyAsBytes())

val Context.userId: String
    get() = this.sessionAttribute(USER_ID)!!

val Context.guildId: String
    get() = this.pathParam(GUILD_ID)

fun Context.fetchGuild(): Guild? {
    return try {
        restJDA.retrieveGuildById(this.guildId).complete()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Context.authOrFail() {
    if (!(this.header("Authorization") != null && duncteApis.validateToken(this.header("Authorization")!!))) {
        throw UnauthorizedResponse()
    }
}

fun Context.getSession(oAuth2Client: OAuth2Client): Session? {
    val sessionId = this.sessionAttribute<String?>(SESSION_ID)

    if (sessionId.isNullOrEmpty()) {
        return null
    }

    return oAuth2Client.sessionController.getSession(sessionId)
}

fun String?.toCBBool(): Boolean = if (this.isNullOrEmpty()) false else (this == "on")

fun String?.toSafeLong(): Long {
    if (this.isNullOrBlank()) {
        return 0L
    }

    return try {
        this.toLong()
    } catch (ignored: NumberFormatException) {
        0L
    }
}

fun haltDiscordError(ctx: Context, error: DiscordError, guildId: String = ""): ForbiddenResponse {
    if (ctx.contentType() == ContentType.JSON) {
        ctx.json(
            jsonMapper.createObjectNode()
                .put("success", false)
                .put("message", error.title)
                .put("code", 403)
        )
    } else {
        VueComponent(error.component, mapOf(
            "title" to error.title,
            "guildId" to guildId,
        )).handle(ctx)
    }

    throw ForbiddenResponse()
}

fun verifyCaptcha(response: String): JsonNode {
    val body = FormBody.Builder()
        .add("secret", System.getenv("CAPTCHA_SECRET"))
        .add("response", response)
        .build()

    httpClient.newCall(
        okhttp3.Request.Builder()
            .url("https://hcaptcha.com/siteverify")
            .post(body)
            .build()
    ).execute().use {
        val readFully = IOUtil.readFully(IOUtil.getBody(it))

        return jsonMapper.readTree(readFully)
    }
}
