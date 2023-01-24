package com.dunctebot.dashboard.controllers.api

import com.dunctebot.dashboard.*
import com.dunctebot.dashboard.WebServer.Companion.SESSION_ID
import com.dunctebot.dashboard.WebServer.Companion.USER_ID
import com.dunctebot.dashboard.constants.ContentType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.UnauthorizedResponse

object CustomCommandController {
    fun before(ctx: Context) {
        val attributes = ctx.sessionAttributeMap()

        if (!(attributes.contains(USER_ID) && attributes.contains(SESSION_ID))) {
            ctx.contentType(ContentType.JSON)
            
            throw UnauthorizedResponse("Invalid session")
        }
    }

    fun show(ctx: Context) {
        val commands = duncteApis.fetchCustomCommands(ctx.guildId.toLong()) as ArrayNode

        val res = jsonMapper.createObjectNode()
            .put("success", true)
            .put("code", ctx.status())
        val arr = res.putArray("commands")

        arr.addAll(commands)

        ctx.json(res)
    }

    fun update(ctx: Context) {
        val commandData = ctx.jsonBody

        if (!commandData.has("invoke") || !commandData.has("message")) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Invalid data")
                    .put("code", ctx.status())
            )

            throw BadRequestResponse()
        }

        val invoke = commandData["invoke"].asText()
        val message = commandData["message"].asText()
        val autoresponse = commandData["autoresponse"].asBoolean(false)

        val guildId = ctx.guildId.toLong()

        // TODO: this should return a 404 error if the command does not exist
        //  (saves a request to the database)
        val returnData = duncteApis.updateCustomCommand(guildId, invoke, message, autoresponse).first

        if (!returnData) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Could not update command")
                    .put("code", ctx.status())
            )

            throw BadRequestResponse()
        }

        val updateData = jsonMapper.createObjectNode()
        updateData.putArray("update")
            .add(
                jsonMapper.createObjectNode()
                    .put("guild_id", guildId)
                    .put("invoke", invoke)
                    .put("message", message)
                    .put("autoresponse", autoresponse)
            )

        sendCustomCommandUpdate(updateData)

        ctx.json(
            jsonMapper.createObjectNode()
                .put("success", true)
                .put("code", ctx.status())
        )
    }

    fun create(ctx: Context) {
        val commandData = ctx.jsonBody

        if (!commandData.has("invoke") || !commandData.has("message") || !commandData.has("autoresponse")) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Invalid data")
                    .put("code", ctx.status())
            )

            throw BadRequestResponse()
        }

        val invoke = commandData["invoke"].asText().replace("\\s".toRegex(), "")

        if (invoke.length > 25) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Invoke is over 25 characters")
                    .put("code", ctx.status())
            )

            return
        }

        val message = commandData["message"].asText()

        if (message.length > 4000) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Message is over 4000 characters")
                    .put("code", ctx.status())
            )

            return
        }

        val guildId = ctx.guildId.toLong()

        // TODO: this check is already done in the create method
        if (duncteApis.fetchCustomCommand(guildId, invoke) != null) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Command already exists")
                    .put("code", ctx.status())
            )

            return
        }

        val autoresponse = commandData["autoresponse"].asBoolean(false)

        val result = duncteApis.createCustomCommand(guildId, invoke, message, autoresponse)

        if (result.first) {
            val updateData = jsonMapper.createObjectNode()
            updateData.putArray("add")
                .add(
                    jsonMapper.createObjectNode()
                        .put("guild_id", guildId)
                        .put("invoke", invoke)
                        .put("message", message)
                        .put("autoresponse", autoresponse)
                )

            sendCustomCommandUpdate(updateData)

            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", true)
                    .put("message", "Command added")
                    .put("code", ctx.status())
            )

            return
        }

        if (result.second) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Command already exists")
                    .put("code", ctx.status())
            )

            return
        }

        if (result.third) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "You reached the limit of 50 custom commands for this server")
                    .put("code", ctx.status())
            )

            return
        }

        ctx.json(
            jsonMapper.createObjectNode()
                .put("success", false)
                .put("message", "Database error")
                .put("code", ctx.status())
        )
    }

    fun delete(ctx: Context) {
        val commandData = ctx.jsonBody

        if (!commandData.has("invoke")) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Invalid data")
                    .put("code", ctx.status())
            )

            return
        }

        val invoke = commandData["invoke"].asText()

        val guildId = ctx.guildId.toLong()

        if (duncteApis.fetchCustomCommand(guildId, invoke) == null) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Command does not exists")
                    .put("code", ctx.status())
            )

            return
        }

        val success = duncteApis.deleteCustomCommand(guildId, invoke).first

        if (!success) {
            ctx.json(
                jsonMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Could not delete command")
                    .put("code", ctx.status())
            )

            return
        }

        val updateData = jsonMapper.createObjectNode()
        updateData.putArray("remove")
            .add(
                jsonMapper.createObjectNode()
                    .put("guild_id", guildId)
                    .put("invoke", invoke)
            )

        sendCustomCommandUpdate(updateData)

        ctx.json(
            jsonMapper.createObjectNode()
                .put("success", true)
                .put("message", "Command deleted")
                .put("code", ctx.status())
        )
    }

    private fun sendCustomCommandUpdate(data: JsonNode) {
        val json = jsonMapper.createObjectNode()
            .put("t", "CUSTOM_COMMANDS")
            .set<ObjectNode>("d", data)

        webSocket.broadcast(json)
    }
}
