package com.dunctebot.dashboard.controllers.api

import com.dunctebot.dashboard.*
import com.dunctebot.dashboard.controllers.GuildController
import com.dunctebot.dashboard.utils.HashUtils
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.javalin.http.Context
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.CompletableFuture

object GuildApiController {
    fun findUserAndGuild(ctx: Context) {
        // TODO: body validator
        val data = ctx.jsonBody

        if (!(data.has("user_id") && data.has("guild_id") && data.has("captcha_response"))) {
            ctx.status(406)
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "missing_input")
                    .put("code", ctx.status())
            )

            return
        }

        if (data["captcha_response"].isNull) {
            ctx.status(406)
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Captcha missing")
                    .put("code", ctx.status())
            )

            return

        }

        val captchaResponse = data["captcha_response"].asText()
        val captchaResult = verifyCaptcha(captchaResponse)

        if (!captchaResult["success"].asBoolean()) {
            ctx.status(403)
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Could not validate that you are a human")
                    .put("code", ctx.status())
            )

            return
        }

        val user: User? = try {
            restJDA.retrieveUserById(data["user_id"].asText()).complete()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (user == null) {
            ctx.status(404)
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "no_user")
                    .put("code", ctx.status())
            )

            return
        }

        val guild: JsonNode? = try {
            val future = CompletableFuture<JsonNode>()

            val json = jsonMapper.createObjectNode()
                json.putArray("partial_guilds")
                .add(data["guild_id"].asText())
            webSocket.requestData(json, future::complete)

            val partialGuilds = future.get()["partial_guilds"]

            if (partialGuilds.isEmpty) {
                null
            } else {
                val respJson = partialGuilds[0]

                if (respJson["member_count"].asInt() == -1) {
                    null
                } else {
                    respJson
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (guild == null) {
            ctx.status(404)
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "no_guild")
                    .put("code", ctx.status())
            )

            return
        }

        val guildId = guild["id"].asText()
        val guildJson = jsonMapper.createObjectNode()
            .put("id", guildId)
            .put("name", guild["name"].asText())

        val userJson = jsonMapper.createObjectNode()
            .put("id", user.id)
            .put("name", user.name)
            .put("formatted", user.asTag)

        val theKey = "${user.idLong}-${guildId}"
        val theHash = HashUtils.sha1(theKey + System.currentTimeMillis())

        GuildController.securityKeys[theHash] = theKey

        val node = jsonMapper.createObjectNode()
            .put("success", true)
            .put("token", theHash)
            .put("code", ctx.status())

        node.set<ObjectNode>("user", userJson)
        node.set<ObjectNode>("guild", guildJson)

        ctx.json(node)
    }
}
