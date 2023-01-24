package com.dunctebot.duncteapi

import com.dunctebot.dashboard.constants.ContentType.JSON
import com.dunctebot.dashboard.httpClient
import com.dunctebot.dashboard.jsonMapper
import com.dunctebot.models.settings.GuildSetting
import com.dunctebot.models.settings.WarnAction
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.io.IOException

// TODO: replace this with database connection
class DuncteApi(val apiKey: String) {
    private val logger = LoggerFactory.getLogger(DuncteApi::class.java)
    val validTokens = mutableListOf<String>()

    fun validateToken(token: String): Boolean {
        if (validTokens.contains(token)) {
            return true
        }

        val json = executeRequest(defaultRequest("validate-token?bot_routes=true&the_token=$token"))
        val isValid = json["success"].asBoolean()

        // cache the valid tokens to make validation faster
        if (isValid) {
            validTokens.add(token)
        }

        return isValid
    }

    fun isOneGuildPatron(userId: String): Boolean {
        val json = executeRequest(defaultRequest("patrons/oneguild/$userId"))

        return !json["data"].isEmpty
    }

    fun saveGuildSetting(setting: GuildSetting) {
        val json = setting.toJson(jsonMapper)

        patchJSONAsync("guildsettings/${setting.guildId}", json) {
            if (!it["success"].asBoolean()) {
                logger.error("Failed to update guild settings for ${setting.guildId}\n" +
                    "Response: {}", it["error"].toString())
            }
        }
    }

    fun updateWarnActions(guildId: Long, actions: List<WarnAction>) {
        val json = jsonMapper.createObjectNode()

        json.putArray("warn_actions")
            .addAll(jsonMapper.valueToTree<ArrayNode>(actions))

        val response = postJSON("guildsettings/$guildId/warn-actions", json)

        if (!response["success"].asBoolean()) {
            logger.error("Failed to set warn actions for $guildId\n" +
                "Response: {}", response["error"].toString())
        }
    }

    fun fetchGuildSetting(guildId: Long): JsonNode {
        return executeRequest(defaultRequest("guildsettings/$guildId"))["data"]
    }

    fun fetchCustomCommands(guildId: Long): JsonNode {
        return executeRequest(defaultRequest("customcommands/$guildId"))["data"]["data"]
    }

    fun fetchCustomCommand(guildId: Long, invoke: String): JsonNode? {
        val resp = executeRequest(defaultRequest("customcommands/$guildId/$invoke"))

        if (!resp["success"].asBoolean()) {
            return null
        }

        return resp["data"]
    }

    fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean): Triple<Boolean, Boolean, Boolean> {
        val json = jsonMapper.createObjectNode()
            .put("message", message)
            .put("autoresponse", autoresponse)

        val response = patchJSON("customcommands/$guildId/$invoke", json)

        return parseTripleResponse(response)
    }

    fun createCustomCommand(guildId: Long, name: String, message: String, autoresponse: Boolean): Triple<Boolean, Boolean, Boolean> {
        val json = jsonMapper.createObjectNode()
            .put("invoke", name)
            .put("message", message)
            .put("autoresponse", autoresponse)

        val response = postJSON("customcommands/$guildId", json)

        return parseTripleResponse(response)
    }

    fun deleteCustomCommand(guildId: Long, invoke: String): Triple<Boolean, Boolean, Boolean> {
        val response = executeRequest(defaultRequest("customcommands/$guildId/$invoke").delete())

        return parseTripleResponse(response)
    }

    fun isPatreon(userId: String): Boolean {
        val request = defaultRequest("")
            .url("https://apis.beta.duncte123.me/bot/patrons/$userId")

        val response = executeRequest(request)

        return response.get("success").asBoolean();
    }

    private fun patchJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = json.toJsonString().toRequestBody()
        val request = defaultRequest(path, prefixBot)
            .patch(body).addHeader("Content-Type", JSON)

        return executeRequest(request)
    }

    private fun patchJSONAsync(path: String, json: JsonNode, prefixBot: Boolean = true, callback: (JsonNode) -> Unit = {}) {
        val body = json.toJsonString().toRequestBody()
        val request = defaultRequest(path, prefixBot)
            .patch(body).addHeader("Content-Type", JSON)

        executeAsyncRequest(request, callback)
    }

    private fun postJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = json.toJsonString().toRequestBody()
        val request = defaultRequest(path, prefixBot)
            .post(body).addHeader("Content-Type", JSON)

        return executeRequest(request)
    }

    private fun deleteJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = json.toJsonString().toRequestBody()
        val request = defaultRequest(path, prefixBot)
            .delete(body).addHeader("Content-Type", JSON)

        return executeRequest(request)
    }

    private fun defaultRequest(path: String, prefixBot: Boolean = true): Request.Builder {
        val prefix = if (prefixBot) "bot/" else ""

        return Request.Builder()
            .url("$API_HOST/$prefix$path")
            .get()
            .addHeader("Authorization", apiKey)
            .addHeader("Accept", JSON)
    }

    private fun executeRequest(request: Request.Builder): JsonNode {
        httpClient.newCall(request.build())
            .execute().use {
                return jsonMapper.readTree(it.body!!.byteStream())
            }
    }

    private fun executeAsyncRequest(request: Request.Builder, callback: (JsonNode) -> Unit) {
        httpClient.newCall(request.build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Error when making api request", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        callback(jsonMapper.readTree(it.body!!.byteStream()))
                    }
                }

            })
    }

    private fun parseTripleResponse(response: JsonNode): Triple<Boolean, Boolean, Boolean> {
        val success = response["success"].asBoolean()

        if (success) {
            return Triple(first = true, second = false, third = false)
        }

        val error = response["error"]
        val type = error["type"].asText()

        if (type == "AmountException") {
            return Triple(first = false, second = false, third = true)
        }

        if (type !== "ValidationException") {
            return Triple(first = false, second = false, third = false)
        }

        val errors = response["error"]["errors"]

        for (key in errors.fieldNames()) {
            errors[key].forEach { reason ->
                if (reason.asText().contains("The invoke has already been taken.")) {
                    return Triple(first = false, second = true, third = false)
                }
            }
        }

        return Triple(first = false, second = false, third = false)
    }

    private fun JsonNode.toJsonString() = jsonMapper.writeValueAsString(this)

    companion object {
        // const val API_HOST = "http://localhost:8081"
//        const val API_HOST = "http://duncte123-apis-lumen.test/"
         const val API_HOST = "https://apis.duncte123.me"
        const val USER_AGENT = "Mozilla/5.0 (compatible; SkyBot/dashboard; +https://dashboard.dunctebot.com)"
    }
}
