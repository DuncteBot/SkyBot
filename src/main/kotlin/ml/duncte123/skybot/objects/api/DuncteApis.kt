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

import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.WebUtils.EncodingType.APPLICATION_JSON
import me.duncte123.weebJava.helpers.IOHelper
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory

@Author(nickname = "duncte123", author = "Duncan Sterken")
class DuncteApis(private val apiKey: String) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getCustomCommands(): JSONArray {
        return paginateData("customcommands")
    }

    fun createCustomCommand(guildId: Long, invoke: String, message: String): Triple<Boolean, Boolean, Boolean> {
        val json = JSONObject().put("invoke", invoke).put("message", message)
        val response = postJSON("customcommands/$guildId", json)

        return parseTripleResponse(response)
    }

    fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean): Triple<Boolean, Boolean, Boolean> {
        val json = JSONObject().put("message", message).put("autoresponse", autoresponse)
        val response = patchJSON("customcommands/$guildId/$invoke", json)

        return parseTripleResponse(response)
    }

    fun deleteCustomCommand(guildId: Long, invoke: String): Boolean {
        val request = defaultRequest("customcommands/$guildId/$invoke").delete()

        return executeRequest(request).getBoolean("success")
    }

    fun restoreCustomCommand(commandId: Int): Boolean {
        val request = defaultRequest("customcommands/$commandId")
            .put(RequestBody.create(null, JSONObject().toString()))
        val response = executeRequest(request)

        if (!response.getBoolean("success")) {
            return false
        }

        val command = response.getJSONObject("data")
        val commandManager = Variables.getInstance().commandManager

        commandManager.customCommands.add(CustomCommandImpl(
            command.getString("invoke"),
            command.getString("message"),
            command.getLong("guildId"),
            command.getBoolean("autoresponse")
        ))

        return true
    }

    fun getGuildSettings(): JSONArray {
        return paginateData("guildsettings")
    }

    fun getGuildSetting(guildId: Long): JSONObject {
        return executeRequest(defaultRequest("guildsettings/$guildId")).getJSONObject("data")
    }

    fun updateGuildSettings(guildSettings: GuildSettings): Boolean {
        val json = guildSettings.toJson()
        val response = patchJSON("guildsettings/${guildSettings.guildId}", json)

        return response.getBoolean("success")
    }

    fun deleteGuildSetting(guildId: Long) {
        val response = executeRequest(defaultRequest("guildsettings/$guildId").delete())

        if (!response.getBoolean("success")) {
            logger.error("Failed to delete guild setting\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun registerNewGuildSettings(guildSettings: GuildSettings): Boolean {
        val json = guildSettings.toJson()
        val response = postJSON("guildsettings", json)
        val success = response.getBoolean("success")

        if (success) {
            return true
        }

        logger.error("Failed to register new guild\n" +
            "Response: {}", response.getJSONObject("error").toString(4))

        return false
    }

    fun addWordToBlacklist(guildId: Long, word: String) {
        val json = JSONObject().put("word", word)
        val response = postJSON("guildsettings/$guildId/blacklist", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to add word to blacklist for guild {}\nResponse: {}",
                guildId, response.getJSONObject("error").toString(4))
        }
    }

    fun removeWordFromBlacklist(guildId: Long, word: String) {
        val json = JSONObject().put("word", word)
        val response = deleteJSON("guildsettings/$guildId/blacklist", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to remove word from blacklist for guild {}\nResponse: {}",
                guildId, response.getJSONObject("error").toString(4))
        }
    }

    fun clearBlacklist(guildId: Long) {
        val request = defaultRequest("guildsettings/$guildId/blacklist/all").delete()
        val response = executeRequest(request)

        if (!response.getBoolean("success")) {
            logger.error("Failed to clear blacklist for guild {}\nResponse: {}",
                guildId, response.getJSONObject("error").toString(4))
        }
    }

    fun loadEmbedSettings(): JSONArray {
        return paginateData("embedsettings")
    }

    fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        val json = JSONObject().put("embed_color", color)
        val response = postJSON("embedsettings/$guildId", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to save embed data\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun loadOneGuildPatrons(): JSONArray {
        return paginateData("patrons/oneguild")
    }

    fun updateOrCreateOneGuildPatron(userId: Long, guildId: Long): Boolean {
        val json = JSONObject().put("user_id", userId.toString()).put("guild_id", guildId.toString())
        val response = postJSON("patrons/oneguild", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to add one guild patron\n" +
                "Response: {}", response.getJSONObject("error").toString(4))

            return false
        }

        return true
    }

    fun getOneGuildPatron(userId: Long): JSONArray {
        val response = executeRequest(defaultRequest("patrons/oneguild/$userId"))

        return response.getJSONArray("data")
    }

    fun removeOneGuildPatron(userId: Long) {
        val response = executeRequest(defaultRequest("patrons/oneguild/$userId").delete())

        if (!response.getBoolean("success")) {
            logger.error("Failed to remove one guild patron\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun createBan(json: JSONObject) {
        val response = postJSON("bans", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to create a ban\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String) {
        val json = JSONObject()
            .put("mod_id", modId.toString())
            .put("user_id", userId.toString())
            .put("guild_id", guildId.toString())
            .put("reason", reason)

        val response = postJSON("warns", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to create a warning\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun createMute(json: JSONObject) {
        val response = postJSON("mutes", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to create a mute\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun getWarningsForUser(userId: Long, guildId: Long): JSONArray {
        val response = executeRequest(defaultRequest("warns/$userId/$guildId"))

        return response.getJSONArray("data")
    }

    fun getExpiredBansAndMutes(): JSONObject {
        val response = executeRequest(defaultRequest("expiredbansandmutes"))

        return response.getJSONObject("data")
    }

    fun purgeBans(ids: List<Int>) {
        val json = JSONObject().put("ids", ids)
        val response = deleteJSON("bans", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to purge bans\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun purgeMutes(ids: List<Int>) {
        val json = JSONObject().put("ids", ids)
        val response = deleteJSON("mutes", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to purge mutes\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun getVcAutoRoles(): JSONArray {
        return paginateData("vcautoroles")
    }

    fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) {
        val json = JSONObject()
            .put("guild_id", guildId.toString())
            .put("voice_channel_id", voiceChannelId.toString())
            .put("role_id", roleId.toString())


        val response = postJSON("vcautoroles", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to set vc autorole\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun removeVcAutoRole(voiceChannelId: Long) {
        val request = defaultRequest("vcautoroles/$voiceChannelId").delete()
        val response = executeRequest(request)

        if (!response.getBoolean("success")) {
            logger.error("Failed to remove vc autorole\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun removeVcAutoRoleForGuild(guildId: Long) {
        val request = defaultRequest("vcautoroles/guild/$guildId").delete()
        val response = executeRequest(request)

        if (!response.getBoolean("success")) {
            logger.error("Failed to remove vc autorole\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun decodeToken(token: String): JSONObject {
        val json = JSONObject().put("token", token)


        return postJSON("token", json)
    }

    fun getPronouns(userId: Long): JSONObject? {
        val json = executeRequest(defaultRequest("pronouns/$userId"))

        if (!json.getBoolean("success")) {
            return null
        }

        return json.getJSONObject("data")
    }

    fun getLove(name: String, name2: String): JSONObject {
        val json = executeRequest(defaultRequest("love/$name/$name2", false))

        return json.getJSONObject("data")
    }

    fun getMeguminQuote(): String {
        val json = executeRequest(defaultRequest("megumin", false))

        return json.getJSONObject("data").getString("quote")
    }

    fun setPronouns(userId: Long, pronouns: String, singular: Boolean) {
        val json = JSONObject()
            .put("pronouns", pronouns)
            .put("singular", singular)

        val response = postJSON("pronouns/$userId", json)

        if (!response.getBoolean("success")) {
            logger.error("Failed to create a pronoun\n" +
                "Response: {}", response.getJSONObject("error").toString(4))
        }
    }

    fun getFlag(flag: String, avatarUrl: String) = getImageRaw("flags", flag, avatarUrl)

    fun getFilter(flag: String, avatarUrl: String) = getImageRaw("filters", flag, avatarUrl)

    private fun getImageRaw(path: String, item: String, avatarUrl: String): ByteArray {
        val json = JSONObject().put("image", avatarUrl)

        return postJSONBytes("$path/$item", json)
    }

    fun getIWantToDie(text: String): ByteArray {
        val json = JSONObject().put("text", text)

        return postJSONBytes("memes/wanttodie", json)
    }

    fun getFreeRealEstate(text: String): ByteArray {
        val json = JSONObject().put("text", text)

        return postJSONBytes("memes/itsfreerealestate", json)
    }

    fun getDannyDrake(top: String, bottom: String, dabbing: Boolean = false): ByteArray {
        val json = JSONObject().put("top", top).put("bottom", bottom).put("dabbing", dabbing)

        return postJSONBytes("memes/dannyphantomdrake", json)
    }

    fun getDrakeMeme(top: String, bottom: String): ByteArray {
        val json = JSONObject().put("top", top).put("bottom", bottom)

        return postJSONBytes("memes/drakememe", json)
    }

    fun getAllTags(): JSONArray {
        return paginateData("tags")
    }

    fun createTag(tag: JSONObject): Pair<Boolean, String> {
        val response = postJSON("tags", tag)

        if (!response.getBoolean("success")) {
            val error = response.getJSONObject("error")

            if (error.getString("type") == "ValidationException") {
                return Pair(false, buildValidationErrorString(error))
            }

            logger.error("Failed to create a tag\n" +
                "Response: {}", error.toString(4))

            return Pair(false, error.getString("message"))
        }

        return Pair(true, "")
    }

    fun deleteTag(tagName: String): Pair<Boolean, String> {
        val response = executeRequest(defaultRequest("tags/$tagName").delete())

        if (!response.getBoolean("success")) {
            val error = response.getJSONObject("error")

            logger.error("Failed to create a tag\n" +
                "Response: {}", error.toString(4))

            return Pair(false, error.getString("message"))
        }

        return Pair(true, "")
    }

    private fun buildValidationErrorString(error: JSONObject): String {
        val errors = error.getJSONObject("errors")

        return buildString {
            errors.keySet().forEach {
                errors.getJSONArray(it).forEach { er ->
                    appendln(er.toString())
                }
            }
        }
    }

    private fun paginateData(path: String): JSONArray {
        val page1 = executeRequest(defaultRequest("$path?page=1")).getJSONObject("data")

        val data = page1.getJSONArray("data")

        if (page1.optString("next_page_url", null) == null) {
            return data
        }

        val totalPages = page1.getInt("last_page") + 1

        for (i in 2 until totalPages) {
            val page = executeRequest(defaultRequest("$path?page=$i")).getJSONObject("data")

            val pageData = page.getJSONArray("data")

            for (i2 in 0 until pageData.length()) {
                data.put(pageData.get(i2))
            }
        }

        return data
    }

    private fun postJSONBytes(path: String, json: JSONObject): ByteArray {
        val body = RequestBody.create(null, json.toString())
        val request = defaultRequest(path, false)
            .post(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return WebUtils.ins.prepareRaw(request.build(), IOHelper::read).execute()
    }

    private fun parseTripleResponse(response: JSONObject): Triple<Boolean, Boolean, Boolean> {
        val success = response.getBoolean("success")

        if (success) {
            return Triple(true, false, false)
        }

        val error = response.getJSONObject("error")

        if (error.getString("type") == "AmountException") {
            return Triple(false, false, true)
        }

        if (error.getString("type") == "ValidationException") {
            val errors = response.getJSONObject("error").getJSONObject("errors")

            for (key in errors.keySet()) {
                val reasons = errors.getJSONArray(key)

                if (reasons.contains("The invoke has already been taken.")) {
                    return Triple(false, true, false)
                }

                if (reasons.contains("The message may not be greater than 4000 characters.")) {
                    return Triple(false, false, false)
                }
            }
        }

        return Triple(false, false, false)
    }

    private fun patchJSON(path: String, json: JSONObject, prefixBot: Boolean = true): JSONObject {
        val body = RequestBody.create(null, json.toString())
        val request = defaultRequest(path, prefixBot)
            .patch(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return executeRequest(request)
    }

    private fun postJSON(path: String, json: JSONObject, prefixBot: Boolean = true): JSONObject {
        val body = RequestBody.create(null, json.toString())
        val request = defaultRequest(path, prefixBot)
            .post(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return executeRequest(request)
    }

    private fun deleteJSON(path: String, json: JSONObject, prefixBot: Boolean = true): JSONObject {
        val body = RequestBody.create(null, json.toString())
        val request = defaultRequest(path, prefixBot)
            .delete(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return executeRequest(request)
    }

    private fun executeRequest(request: Request.Builder): JSONObject {
        return WebUtils.ins.prepareRaw(request.build(), WebParserUtils::toJSONObject).execute()
    }

    private fun defaultRequest(path: String, prefixBot: Boolean = true): Request.Builder {
        val prefix = if (prefixBot) "bot/" else ""

        return WebUtils.defaultRequest()
            .url("$API_HOST/$prefix$path")
            .get()
            .addHeader("Authorization", apiKey)
    }


    fun executeDefaultGetRequest(path: String, prefixBot: Boolean = true): JSONObject {
        return executeRequest(
            defaultRequest(path, prefixBot)
        )
    }

    companion object {
        const val API_HOST = "https://apis.duncte123.me"
//        const val API_HOST = "http://duncte123-apis-lumen.local"
    }
}
