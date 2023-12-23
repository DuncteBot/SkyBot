/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot.objects.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.natanbc.reliqua.limiter.RateLimiter
import me.duncte123.botcommons.web.ContentType.JSON
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.WebUtils.urlEncodeString
import me.duncte123.weebJava.helpers.IOHelper
import me.duncte123.skybot.objects.command.CustomCommand
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.util.*

class DuncteApis(val apiKey: String, private val mapper: ObjectMapper) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun restoreCustomCommand(commandId: Int): Pair<Boolean, CustomCommand?> {
        val request = defaultRequest("customcommands/$commandId")
            .put("{}".toRequestBody(null))
        val response = executeRequest(request)

        if (!response["success"].asBoolean()) {
            return false to null
        }

        val command = response["data"]

        return true to CustomCommand(
            command["invoke"].asText(),
            command["message"].asText(),
            command["guildId"].asLong(),
            command["autoresponse"].asBoolean()
        )
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

    fun getAnimal(type: String): JsonNode {
        return executeDefaultGetRequest("animal/$type", false)["data"]
    }

    fun getOrlyImage(): String {
        return executeDefaultGetRequest("orly", false)["data"].asText()
    }

    fun getRCGUrl(): Pair<String, String>? {
        val response = executeRequest(defaultRequest("images/rcg/random-v2", false))

        if (!response["success"].asBoolean()) {
            return null
        }

        val data = response["data"]

        return data["image"].asText() to data["page"].asText()
    }

    private fun postJSONBytes(path: String, json: JsonNode): Pair<ByteArray?, JsonNode?> {
        val body = json.toJsonString().toRequestBody(null)
        val request = defaultRequest(path, false)
            .post(body).addHeader("Content-Type", JSON.type)

        return WebUtils.ins.prepareBuilder(request, { it.setRateLimiter(RateLimiter.directLimiter()) }, null)
            .build({
                if (it.header("Content-Type") == "application/json") {
                    return@build null to mapper.readTree(it.body!!.byteStream())
                }

                return@build IOHelper.read(it) to null
            }, WebParserUtils::handleError)
            .execute()
    }

    private fun postJSON(path: String, json: JsonNode, prefixBot: Boolean = true): JsonNode {
        val body = json.toJsonString().toRequestBody(null)
        val request = defaultRequest(path, prefixBot)
            .post(body).addHeader("Content-Type", JSON.type)

        return executeRequest(request)
    }

    private fun executeRequest(request: Request.Builder): JsonNode {
        return WebUtils.ins.prepareBuilder(
            request,
            {
                it.setRateLimiter(RateLimiter.directLimiter())
            },
            null
        )
            .build({ mapper.readTree(it.body!!.byteStream()) }, WebParserUtils::handleError)
            .execute()
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
