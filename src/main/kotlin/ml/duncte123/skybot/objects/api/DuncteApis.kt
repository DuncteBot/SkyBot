/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.WebUtils.EncodingType.APPLICATION_JSON
import me.duncte123.weebJava.helpers.IOHelper
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory

@Author(nickname = "duncte123", author = "Duncan Sterken")
class DuncteApis(private val apiKey: String, private val mapper: ObjectMapper) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getCustomCommands(): ArrayNode {
        return paginateData("customcommands")
    }

    fun createCustomCommand(guildId: Long, invoke: String, message: String): Triple<Boolean, Boolean, Boolean> {
        val json = mapper.createObjectNode().put("invoke", invoke).put("message", message)
        val response = postJSON("customcommands/$guildId", json)

        return parseTripleResponse(response)
    }

    fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean): Triple<Boolean, Boolean, Boolean> {
        val json = mapper.createObjectNode().put("message", message).put("autoresponse", autoresponse)
        val response = patchJSON("customcommands/$guildId/$invoke", json)

        return parseTripleResponse(response)
    }

    fun deleteCustomCommand(guildId: Long, invoke: String): Boolean {
        val request = defaultRequest("customcommands/$guildId/$invoke").delete()

        return executeRequest(request).get("success").asBoolean()
    }

    fun restoreCustomCommand(commandId: Int): Boolean {
        val request = defaultRequest("customcommands/$commandId")
            .put(RequestBody.create(null, "{}"))
        val response = executeRequest(request)

        if (!response.get("success").asBoolean()) {
            return false
        }

        val command = response.get("data")
        val commandManager = Variables.getInstance().commandManager

        commandManager.customCommands.add(CustomCommandImpl(
            command.get("invoke").asText(),
            command.get("message").asText(),
            command.get("guildId").asLong(),
            command.get("autoresponse").asBoolean()
        ))

        return true
    }

    fun getGuildSettings(): ArrayNode {
        return paginateData("guildsettings")
    }

    fun getGuildSetting(guildId: Long): JsonNode {
        return executeRequest(defaultRequest("guildsettings/$guildId")).get("data")
    }

    fun updateGuildSettings(guildSettings: GuildSettings): Boolean {
        val json = guildSettings.toJson(mapper)
        val response = patchJSON("guildsettings/${guildSettings.guildId}", json)

        return response.get("success").asBoolean()
    }

    fun deleteGuildSetting(guildId: Long) {
        val response = executeRequest(defaultRequest("guildsettings/$guildId").delete())

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to delete guild setting\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun registerNewGuildSettings(guildSettings: GuildSettings): Boolean {
        val json = guildSettings.toJson(mapper)
        val response = postJSON("guildsettings", json)
        val success = response.get("success").asBoolean()

        if (success) {
            return true
        }

        logger.error("Failed to register new guild\n" +
            "Response: {}", response.get("error").toString())

        return false
    }

    fun addWordToBlacklist(guildId: Long, word: String) {
        val json = mapper.createObjectNode().put("word", word)
        val response = postJSON("guildsettings/$guildId/blacklist", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to add word to blacklist for guild {}\nResponse: {}",
                guildId, response.get("error").toString())
        }
    }

    fun removeWordFromBlacklist(guildId: Long, word: String) {
        val json = mapper.createObjectNode().put("word", word)
        val response = deleteJSON("guildsettings/$guildId/blacklist", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to remove word from blacklist for guild {}\nResponse: {}",
                guildId, response.get("error").toString())
        }
    }

    fun clearBlacklist(guildId: Long) {
        val request = defaultRequest("guildsettings/$guildId/blacklist/all").delete()
        val response = executeRequest(request)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to clear blacklist for guild {}\nResponse: {}",
                guildId, response.get("error").toString())
        }
    }

    fun loadEmbedSettings(): ArrayNode {
        return paginateData("embedsettings")
    }

    fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        val json = mapper.createObjectNode().put("embed_color", color)
        val response = postJSON("embedsettings/$guildId", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to save embed data\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun loadOneGuildPatrons(): ArrayNode {
        return paginateData("patrons/oneguild")
    }

    fun updateOrCreateOneGuildPatron(userId: Long, guildId: Long): Boolean {
        val json = mapper.createObjectNode()
            .put("user_id", userId.toString()).put("guild_id", guildId.toString())
        val response = postJSON("patrons/oneguild", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to add one guild patron\n" +
                "Response: {}", response.get("error").toString())

            return false
        }

        return true
    }

    fun getOneGuildPatron(userId: Long): ArrayNode {
        val response = executeRequest(defaultRequest("patrons/oneguild/$userId"))

        return response.get("data") as ArrayNode
    }

    fun removeOneGuildPatron(userId: Long) {
        val response = executeRequest(defaultRequest("patrons/oneguild/$userId").delete())

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to remove one guild patron\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun createBan(json: JsonNode) {
        val response = postJSON("bans", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to create a ban\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String) {
        val json = mapper.createObjectNode()
            .put("mod_id", modId.toString())
            .put("user_id", userId.toString())
            .put("guild_id", guildId.toString())
            .put("reason", reason)

        val response = postJSON("warns", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to create a warning\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun createMute(json: JsonNode) {
        val response = postJSON("mutes", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to create a mute\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun getWarningsForUser(userId: Long, guildId: Long): ArrayNode {
        val response = executeRequest(defaultRequest("warns/$userId/$guildId"))

        return response.get("data") as ArrayNode
    }

    fun getExpiredBansAndMutes(): JsonNode {
        val response = executeRequest(defaultRequest("expiredbansandmutes"))

        return response.get("data")
    }

    fun purgeBans(ids: List<Int>) {
        val json = mapper.createObjectNode()
        val arr = json.putArray("ids")

        ids.forEach { arr.add(it) }

        val response = deleteJSON("bans", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to purge bans\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun purgeMutes(ids: List<Int>) {
        val json = mapper.createObjectNode()
        val arr = json.putArray("ids")

        ids.forEach { arr.add(it) }

        val response = deleteJSON("mutes", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to purge mutes\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun getVcAutoRoles(): ArrayNode {
        return paginateData("vcautoroles")
    }

    fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) {
        val json = mapper.createObjectNode()
            .put("guild_id", guildId.toString())
            .put("voice_channel_id", voiceChannelId.toString())
            .put("role_id", roleId.toString())


        val response = postJSON("vcautoroles", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to set vc autorole\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun removeVcAutoRole(voiceChannelId: Long) {
        val request = defaultRequest("vcautoroles/$voiceChannelId").delete()
        val response = executeRequest(request)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to remove vc autorole\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun removeVcAutoRoleForGuild(guildId: Long) {
        val request = defaultRequest("vcautoroles/guild/$guildId").delete()
        val response = executeRequest(request)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to remove vc autorole\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun decodeToken(token: String): JsonNode {
        val json = mapper.createObjectNode().put("token", token)

        return postJSON("token", json)
    }

    fun getPronouns(userId: Long): JsonNode? {
        val json = executeRequest(defaultRequest("pronouns/$userId"))

        if (!json.get("success").asBoolean()) {
            return null
        }

        return json.get("data")
    }

    fun getLove(name: String, name2: String): JsonNode {
        val json = executeRequest(defaultRequest("love/$name/$name2", false))

        return json.get("data")
    }

    fun getMeguminQuote(): String {
        val json = executeRequest(defaultRequest("megumin", false))

        return json.get("data").get("quote").asText()
    }

    fun setPronouns(userId: Long, pronouns: String, singular: Boolean) {
        val json = mapper.createObjectNode()
            .put("pronouns", pronouns)
            .put("singular", singular)

        val response = postJSON("pronouns/$userId", json)

        if (!response.get("success").asBoolean()) {
            logger.error("Failed to create a pronoun\n" +
                "Response: {}", response.get("error").toString())
        }
    }

    fun getFlag(flag: String, avatarUrl: String) = getImageRaw("flags", flag, avatarUrl)

    fun getFilter(flag: String, avatarUrl: String) = getImageRaw("filters", flag, avatarUrl)

    private fun getImageRaw(path: String, item: String, avatarUrl: String): ByteArray {
        val json = mapper.createObjectNode().put("image", avatarUrl)

        return postJSONBytes("$path/$item", json)
    }

    fun getIWantToDie(text: String): ByteArray {
        val json = mapper.createObjectNode().put("text", text)

        return postJSONBytes("memes/wanttodie", json)
    }

    fun getFreeRealEstate(text: String): ByteArray {
        val json = mapper.createObjectNode().put("text", text)

        return postJSONBytes("memes/itsfreerealestate", json)
    }

    fun getDannyDrake(top: String, bottom: String, dabbing: Boolean = false): ByteArray {
        val json = mapper.createObjectNode()
            .put("top", top).put("bottom", bottom).put("dabbing", dabbing)

        return postJSONBytes("memes/dannyphantomdrake", json)
    }

    fun getDrakeMeme(top: String, bottom: String): ByteArray {
        val json = mapper.createObjectNode()
            .put("top", top).put("bottom", bottom)

        return postJSONBytes("memes/drakememe", json)
    }

    fun getAllTags(): ArrayNode {
        return paginateData("tags")
    }

    fun createTag(tag: ObjectNode): Pair<Boolean, String> {
        val response = postJSON("tags", tag)

        if (!response.get("success").asBoolean()) {
            val error = response.get("error") as ObjectNode

            if (error.get("type").asText() == "ValidationException") {
                return Pair(false, buildValidationErrorString(error))
            }

            logger.error("Failed to create a tag\n" +
                "Response: {}", error.toString())

            return Pair(false, error.get("message").asText())
        }

        return Pair(true, "")
    }

    fun deleteTag(tagName: String): Pair<Boolean, String> {
        val response = executeRequest(defaultRequest("tags/$tagName").delete())

        if (!response.get("success").asBoolean()) {
            val error = response.get("error")

            logger.error("Failed to create a tag\n" +
                "Response: {}", error.toString())

            return Pair(false, error.get("message").asText())
        }

        return Pair(true, "")
    }

    private fun buildValidationErrorString(error: ObjectNode): String {
        val errors = error.get("errors")

        return buildString {
            errors.fieldNames().forEach {
                errors.get(it).forEach { er ->
                    appendln(er.toString())
                }
            }
        }
    }

    private fun paginateData(path: String): ArrayNode {
        val page1 = executeRequest(defaultRequest("$path?page=1")).get("data")

        val data = page1.get("data") as ArrayNode

        val totalPages = page1.get("last_page").asInt() + 1

        for (i in 2 until totalPages) {
            val page = executeRequest(defaultRequest("$path?page=$i")).get("data")

            val pageData = page.get("data") as ArrayNode

            data.addAll(pageData)

            /*for (i2 in 0 until pageData.length()) {
                data.addAll(pageData.get(i2))
            }*/
        }

        return data
    }

    private fun postJSONBytes(path: String, json: JsonNode): ByteArray {
        val body = RequestBody.create(null, json.toJsonString())
        val request = defaultRequest(path, false)
            .post(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return WebUtils.ins.prepareRaw(request.build(), IOHelper::read).execute()
    }

    private fun parseTripleResponse(response: JsonNode): Triple<Boolean, Boolean, Boolean> {
        val success = response.get("success").asBoolean()

        if (success) {
            return Triple(first = true, second = false, third = false)
        }

        val error = response.get("error")
        val type = error.get("type").asText()

        if (type == "AmountException") {
            return Triple(first = false, second = false, third = true)
        }

        if (type !== "ValidationException") {
            return Triple(first = false, second = false, third = false)
        }

        val errors = response.get("error").get("errors")

        for (key in errors.fieldNames()) {
            errors.get(key).forEach { reason ->
                if (reason.asText().contains("The invoke has already been taken.")) {
                    return Triple(first = false, second = true, third = false)
                }
            }
        }

        return Triple(first = false, second = false, third = false)
    }

    private fun patchJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = RequestBody.create(null, json.toJsonString())
        val request = defaultRequest(path, prefixBot)
            .patch(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return executeRequest(request)
    }

    private fun postJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = RequestBody.create(null, json.toJsonString())
        val request = defaultRequest(path, prefixBot)
            .post(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return executeRequest(request)
    }

    private fun deleteJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = RequestBody.create(null, json.toJsonString())
        val request = defaultRequest(path, prefixBot)
            .delete(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return executeRequest(request)
    }

    private fun executeRequest(request: Request.Builder): JsonNode {
        return WebUtils.ins.prepareRaw(request.build()) { mapper.readTree(it.body()!!.byteStream()) }.execute()
    }

    private fun defaultRequest(path: String, prefixBot: Boolean = true): Request.Builder {
        val prefix = if (prefixBot) "bot/" else ""

        return WebUtils.defaultRequest()
            .url("$API_HOST/$prefix$path")
            .get()
            .addHeader("Authorization", apiKey)
    }

    fun executeDefaultGetRequest(path: String, prefixBot: Boolean = true): JsonNode {
        return executeRequest(
            defaultRequest(path, prefixBot)
        )
    }

    private fun JsonNode.toJsonString() = mapper.writeValueAsString(this)

    companion object {
        const val API_HOST = "https://apis.duncte123.me"
//        const val API_HOST = "http://duncte123-apis-lumen.local"
    }
}
