/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import com.dunctebot.models.settings.GuildSetting
import com.dunctebot.models.settings.WarnAction
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import me.duncte123.botcommons.web.ContentType.JSON
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.WebUtils.urlEncodeString
import me.duncte123.weebJava.helpers.IOHelper
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.api.sharding.ShardManager
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
class DuncteApis(val apiKey: String, private val mapper: ObjectMapper) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getCustomCommands(): ArrayNode {
        return paginateData("customcommands")
    }

    fun createCustomCommand(guildId: Long, invoke: String, message: String): Triple<Boolean, Boolean, Boolean> {
        val json = mapper.createObjectNode().put("invoke", invoke).put("message", message)
        val response = postJSON("customcommands/$guildId", json)

        return parseTripleResponse(response)
    }

    fun updateCustomCommand(
        guildId: Long,
        invoke: String,
        message: String,
        autoresponse: Boolean
    ): Triple<Boolean, Boolean, Boolean> {
        val json = mapper.createObjectNode().put("message", message).put("autoresponse", autoresponse)
        val response = patchJSON("customcommands/$guildId/$invoke", json)

        return parseTripleResponse(response)
    }

    fun deleteCustomCommand(guildId: Long, invoke: String): Boolean {
        val request = defaultRequest("customcommands/$guildId/$invoke").delete()

        return executeRequest(request)["success"].asBoolean()
    }

    fun restoreCustomCommand(commandId: Int, variables: Variables): Boolean {
        val request = defaultRequest("customcommands/$commandId")
            .put(RequestBody.create(null, "{}"))
        val response = executeRequest(request)

        if (!response["success"].asBoolean()) {
            return false
        }

        val command = response["data"]
        val commandManager = variables.commandManager

        commandManager.customCommands.add(
            CustomCommandImpl(
                command["invoke"].asText(),
                command["message"].asText(),
                command["guildId"].asLong(),
                command["autoresponse"].asBoolean()
            )
        )

        return true
    }

    fun getGuildSettings(): ArrayNode {
        return paginateData("guildsettings")
    }

    fun getGuildSetting(guildId: Long): JsonNode? {
        val res = executeRequest(defaultRequest("guildsettings/$guildId"))

        if (!res["success"].asBoolean()) {
            return null
        }

        return res["data"]
    }

    fun updateGuildSettings(guildSettings: GuildSetting): Boolean {
        val json = guildSettings.toJson(mapper)
        val response = patchJSON("guildsettings/${guildSettings.guildId}", json)

        return response["success"].asBoolean()
    }

    fun deleteGuildSetting(guildId: Long) {
        val response = executeRequest(defaultRequest("guildsettings/$guildId").delete())

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to delete guild setting\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun registerNewGuildSettings(guildSettings: GuildSetting): Boolean {
        val json = guildSettings.toJson(mapper)
        val response = postJSON("guildsettings", json)
        val success = response["success"].asBoolean()

        if (!success) {
            logger.error(
                "Failed to register new guild\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }

        return success
    }

    fun addWordToBlacklist(guildId: Long, word: String) {
        val json = mapper.createObjectNode().put("word", word)
        val response = postJSON("guildsettings/$guildId/blacklist", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to add word to blacklist for guild {}\nResponse: {}",
                guildId,
                response["error"].toString()
            )
        }
    }

    fun addBatchToBlacklist(guildId: Long, words: List<String>) {
        val json = mapper.createObjectNode()
        val array = json.putArray("words")
        words.forEach { array.add(it) }
        val response = postJSON("guildsettings/$guildId/blacklist/batch", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to batch add to blacklist for guild {}\nResponse: {}",
                guildId,
                response["error"].toString()
            )
        }
    }

    fun removeWordFromBlacklist(guildId: Long, word: String) {
        val json = mapper.createObjectNode().put("word", word)
        val response = deleteJSON("guildsettings/$guildId/blacklist", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to remove word from blacklist for guild {}\nResponse: {}",
                guildId,
                response["error"].toString()
            )
        }
    }

    fun clearBlacklist(guildId: Long) {
        val request = defaultRequest("guildsettings/$guildId/blacklist/all").delete()
        val response = executeRequest(request)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to clear blacklist for guild {}\nResponse: {}",
                guildId,
                response["error"].toString()
            )
        }
    }

    fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        val json = mapper.createObjectNode().put("embed_color", color)
        val response = postJSON("embedsettings/$guildId", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to save embed data\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun loadAllPatrons(): ArrayNode {
        return paginateData("patrons")
    }

    fun createOrUpdatePatron(patron: Patron) {
        val json = mapper.createObjectNode()
            .put("user_id", patron.userId.toString())
            .put("type", patron.type.name)

        if (patron.guildId != null) {
            json.put("guild_id", patron.guildId.toString())
        }

        val response = postJSON("patrons", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to create or update a patron\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun deletePatron(userId: Long) {
        val request = defaultRequest("patrons/$userId").delete()
        val response = executeRequest(request)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to delete a patron\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun updateOrCreateOneGuildPatron(userId: Long, guildId: Long): Boolean {
        val json = mapper.createObjectNode()
            .put("user_id", userId.toString()).put("guild_id", guildId.toString())
        val response = postJSON("patrons/oneguild", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to add one guild patron\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }

        return response["success"].asBoolean()
    }

    fun getOneGuildPatron(userId: Long): JsonNode? {
        val response = executeRequest(defaultRequest("patrons/oneguild/$userId"))

        if (!response["success"].asBoolean()) {
            return null
        }

        val patrons = response["data"]

        if (patrons.isEmpty) {
            return null
        }

        return patrons[0]
    }

    fun createBan(json: JsonNode) {
        val response = postJSON("bans", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to create a ban\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String) {
        val json = mapper.createObjectNode()
            .put("mod_id", modId.toString())
            .put("user_id", userId.toString())
            .put("guild_id", guildId.toString())
            .put("reason", reason)

        val response = postJSON("warns", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to create a warning\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun createMute(json: JsonNode): JsonNode {
        val response = postJSON("mutes", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to create a mute\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }

        return response["data"]
    }

    fun getWarningsForUser(userId: Long, guildId: Long): ArrayNode {
        val response = executeRequest(defaultRequest("warns/$userId/$guildId"))

        return response["data"] as ArrayNode
    }

    fun getWarningCountForUser(userId: Long, guildId: Long): Int {
        val response = executeRequest(defaultRequest("warns/$userId/$guildId/count"))

        return response["data"].asInt(0)
    }

    fun removeLatestWarningForUser(userId: Long, guildId: Long): JsonNode? {
        val response = executeRequest(defaultRequest("warns/$userId/$guildId/latest").delete())

        if (!response["success"].asBoolean() && response["error"]["type"].asText() == "WarningNotFoundException") {
            return null
        }

        return response["data"]
    }

    fun setWarnActions(guildId: Long, warnActions: List<WarnAction>) {
        val json = mapper.createObjectNode()

        json.putArray("warn_actions")
            .addAll(mapper.valueToTree<ArrayNode>(warnActions))

        val response = postJSON("guildsettings/$guildId/warn-actions", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to set warn actions for $guildId\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun purgeBans(ids: List<Int>) {
        val json = mapper.createObjectNode()
        val arr = json.putArray("ids")

        ids.forEach { arr.add(it) }

        val response = deleteJSON("bans", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to purge bans\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun purgeMutes(ids: List<Int>) {
        val json = mapper.createObjectNode()
        val arr = json.putArray("ids")

        ids.forEach { arr.add(it) }

        val response = deleteJSON("mutes", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to purge mutes\n" +
                    "Response: {}",
                response["error"].toString()
            )
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

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to set vc autorole\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long) {
        val json = mapper.createObjectNode()
            .put("role_id", roleId.toString())
        val array = json.putArray("voice_channel_ids")
        voiceChannelIds.forEach { array.add(it) }

        val response = postJSON("vcautoroles/$guildId", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to set vc autorole in batch\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun removeVcAutoRole(voiceChannelId: Long) {
        val request = defaultRequest("vcautoroles/$voiceChannelId").delete()
        val response = executeRequest(request)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to remove vc autorole\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun removeVcAutoRoleForGuild(guildId: Long) {
        val request = defaultRequest("vcautoroles/guild/$guildId").delete()
        val response = executeRequest(request)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to remove vc autorole\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun decodeToken(token: String): JsonNode {
        val json = mapper.createObjectNode().put("token", token)

        return postJSON("token", json, false)
    }

    fun getPronouns(userId: Long): JsonNode? {
        val json = executeRequest(defaultRequest("pronouns/$userId"))

        if (!json["success"].asBoolean()) {
            return null
        }

        return json["data"]
    }

    fun getLove(name: String, name2: String): JsonNode? {
        val json = executeRequest(
            defaultRequest(
                "love/${name.enc()}/${name2.enc()}",
                false
            )
        )

        if (!json["success"].asBoolean()) {
            logger.error(
                "Failed to get love\n" +
                    "Response: {}",
                json["error"].toString()
            )

            return null
        }

        return json["data"]
    }

    fun getMeguminQuote(): String {
        val json = executeRequest(defaultRequest("megumin", false))

        return json["data"]["quote"].asText()
    }

    fun setPronouns(userId: Long, pronouns: String, singular: Boolean) {
        val json = mapper.createObjectNode()
            .put("pronouns", pronouns)
            .put("singular", singular)

        val response = postJSON("pronouns/$userId", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to create a pronoun\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun getFlag(flag: String, avatarUrl: String) = getImageRaw("flags", flag, avatarUrl)

    fun getFilter(flag: String, avatarUrl: String) = getImageRaw("filters", flag, avatarUrl)

    fun screenshotWebsite(url: String): ByteArray {
        val response = executeRequest(defaultRequest("screenshot?url=${url.enc()}"))

        return Base64.getDecoder().decode(response["data"].asText())
    }

    private fun getImageRaw(path: String, item: String, avatarUrl: String): Pair<ByteArray?, JsonNode?> {
        val json = mapper.createObjectNode().put("image", avatarUrl)

        return postJSONBytes("$path/$item", json)
    }

    fun getIWantToDie(text: String): Pair<ByteArray?, JsonNode?> {
        val json = mapper.createObjectNode().put("text", text)

        return postJSONBytes("memes/wanttodie", json)
    }

    fun getFreeRealEstate(text: String): Pair<ByteArray?, JsonNode?> {
        val json = mapper.createObjectNode().put("text", text)

        return postJSONBytes("memes/itsfreerealestate", json)
    }

    fun getDannyDrake(top: String, bottom: String, dabbing: Boolean = false): Pair<ByteArray?, JsonNode?> {
        val json = mapper.createObjectNode()
            .put("top", top).put("bottom", bottom).put("dabbing", dabbing)

        return postJSONBytes("memes/dannyphantomdrake", json)
    }

    fun getDrakeMeme(top: String, bottom: String): Pair<ByteArray?, JsonNode?> {
        val json = mapper.createObjectNode()
            .put("top", top).put("bottom", bottom)

        return postJSONBytes("memes/drakememe", json)
    }

    fun getAllTags(): ArrayNode {
        return paginateData("tags")
    }

    fun createTag(tag: ObjectNode): Pair<Boolean, String> {
        val response = postJSON("tags", tag)

        if (!response["success"].asBoolean()) {
            val error = response["error"] as ObjectNode

            if (error["type"].asText() == "ValidationException") {
                return false to buildValidationErrorString(error)
            }

            logger.error(
                "Failed to create a tag\n" +
                    "Response: {}",
                error.toString()
            )

            return false to error["message"].asText()
        }

        return true to ""
    }

    fun deleteTag(tagName: String): Pair<Boolean, String> {
        val response = executeRequest(defaultRequest("tags/$tagName").delete())

        if (!response["success"].asBoolean()) {
            val error = response["error"]

            logger.error(
                "Failed to create a tag\n" +
                    "Response: {}",
                error.toString()
            )

            return false to error["message"].asText()
        }

        return true to ""
    }

    fun createReminder(
        userId: Long,
        reminder: String,
        expireDate: String,
        channelId: Long,
        messageId: Long,
        guildId: Long,
        inChannel: Boolean
    ): Pair<Boolean, Int> {
        val obj = mapper.createObjectNode()
            .put("user_id", userId.toString())
            .put("channel_id", channelId.toString())
            .put("guild_id", guildId.toString())
            .put("message_id", messageId.toString())
            .put("in_channel", inChannel)
            .put("reminder", reminder)
            .put("remind_date", expireDate)
            .put("remind_create_date", AirUtils.getDatabaseDateFormat(OffsetDateTime.now(ZoneOffset.UTC)))

        val response = postJSON("reminders", obj)

        if (!response["success"].asBoolean()) {
            val error = response["error"]

            logger.error(
                "Failed to create a reminder\n" +
                    "Response: {}",
                error.toString()
            )

            return false to -1
        }

        return true to response["data"]["id"].asInt()
    }

    fun listReminders(userId: Long): JsonNode {
        val response = executeRequest(defaultRequest("reminders/$userId"))

        if (!response["success"].asBoolean()) {
            val error = response["error"]

            logger.error(
                "Failed to get reminders for user\n" +
                    "Response: {}",
                error.toString()
            )

            // Can't use that as jackson will make the list null
//            return NullNode.instance
            return mapper.createArrayNode()
        }

        return response["data"]
    }

    fun showReminder(userId: Long, reminderId: Int): JsonNode {
        val response = executeRequest(defaultRequest("reminders/$userId/$reminderId"))

        if (!response["success"].asBoolean()) {
            val error = response["error"]

            logger.error(
                "Failed to get reminders for user\n" +
                    "Response: {}",
                error.toString()
            )

            // NOTE: Jackson will make this null when we parse it
            return NullNode.instance
        }

        return response["data"]
    }

    fun purgeReminders(ids: List<Int>) {
        val json = mapper.createObjectNode()
        val arr = json.putArray("ids")

        ids.forEach { arr.add(it) }

        val response = deleteJSON("reminders/purge", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to purge reminders\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun deleteReminder(userId: Long, reminderId: Int): Boolean {
        val response = executeRequest(defaultRequest("reminders/$userId/$reminderId").delete())

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to delete reminder\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }

        return response["success"].asBoolean()
    }

    fun getAnimal(type: String): JsonNode {
        return executeDefaultGetRequest("animal/$type", false)["data"]
    }

    fun getOrlyImage(): String {
        return executeDefaultGetRequest("orly", false)["data"].asText()
    }

    fun sendServerCountToLists(shardManager: ShardManager) {
        val json = mapper.createObjectNode()
            .put("bot_id", shardManager.shardCache.first().selfUser.id)
            .put("shard_count", shardManager.shardCache.size())
            .put("server_count", shardManager.guildCache.size())

        val response = postJSON("guild-count", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to update guild count\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun createBanBypass(guildId: Long, userId: Long) {
        val json = mapper.createObjectNode()
            .put("user_id", userId.toString())
            .put("guild_id", guildId.toString())

        val response = postJSON("bans/bypass", json)

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to create ban bypass\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }
    }

    fun getBanBypass(guildId: Long, userId: Long): JsonNode {
        val response = executeRequest(defaultRequest("bans/bypass/$guildId/$userId"))

        if (!response["success"].asBoolean()) {
            return NullNode.instance
        }

        return response["data"]
    }

    fun deleteBanBypass(guildId: Long, userId: Long): Boolean {
        val response = executeRequest(defaultRequest("bans/bypass/$guildId/$userId").delete())

        if (!response["success"].asBoolean()) {
            logger.error(
                "Failed to delete ban bypass\n" +
                    "Response: {}",
                response["error"].toString()
            )
        }

        return response["success"].asBoolean()
    }

    private fun buildValidationErrorString(error: ObjectNode): String {
        val errors = error["errors"]

        return buildString {
            errors.fieldNames().forEach {
                errors[it].forEach { er ->
                    appendLine(er.toString())
                }
            }
        }
    }

    private fun paginateData(path: String): ArrayNode {
        val res = executeRequest(defaultRequest("$path?page=1"))

        val page1 = res["data"]

        val data = page1["data"] as ArrayNode

        val totalPages = page1["last_page"].asInt() + 1

        for (i in 2 until totalPages) {
            val page = executeRequest(defaultRequest("$path?page=$i"))["data"]

            val pageData = page["data"] as ArrayNode

            data.addAll(pageData)

            /*for (i2 in 0 until pageData.length()) {
                data.addAll(pageData[i2])
            }*/
        }

        return data
    }

    private fun postJSONBytes(path: String, json: JsonNode): Pair<ByteArray?, JsonNode?> {
        val body = RequestBody.create(null, json.toJsonString())
        val request = defaultRequest(path, false)
            .post(body).addHeader("Content-Type", JSON.type)

        return WebUtils.ins.prepareRaw(request.build()) {
            if (it.header("Content-Type") == "application/json") {
                return@prepareRaw null to mapper.readTree(it.body()!!.byteStream())
            }

            return@prepareRaw IOHelper.read(it) to null
        }.execute()
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

    private fun patchJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = RequestBody.create(null, json.toJsonString())
        val request = defaultRequest(path, prefixBot)
            .patch(body).addHeader("Content-Type", JSON.type)

        return executeRequest(request)
    }

    private fun postJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = RequestBody.create(null, json.toJsonString())
        val request = defaultRequest(path, prefixBot)
            .post(body).addHeader("Content-Type", JSON.type)

        return executeRequest(request)
    }

    private fun deleteJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = RequestBody.create(null, json.toJsonString())
        val request = defaultRequest(path, prefixBot)
            .delete(body).addHeader("Content-Type", JSON.type)

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

    private fun String.enc() = urlEncodeString(this)

    companion object {

//        const val API_HOST = "http://localhost:8081"
//        const val API_HOST = "http://duncte123-apis-lumen.test/"
        const val API_HOST = "https://apis.duncte123.me"
    }
}
