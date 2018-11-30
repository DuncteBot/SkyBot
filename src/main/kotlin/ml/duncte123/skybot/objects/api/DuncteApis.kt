/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.WebUtils.EncodingType.APPLICATION_JSON
import me.duncte123.botcommons.web.WebUtilsErrorUtils
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject


class DuncteApis(private val apiKey: String) {

    fun getCustomCommands(): JSONArray {
        return paginateData("customcommands")
    }

    fun createCustomCommand(guildId: Long, invoke: String, message: String): Triple<Boolean, Boolean, Boolean> {
        val json = JSONObject().put("invoke", invoke).put("message", message)
        val response = postJSON("customcommands/$guildId", json)

        return parseTripleResponse(response)
    }

    fun updateCustomCommand(guildId: Long, invoke: String, message: String): Triple<Boolean, Boolean, Boolean> {
        val json = JSONObject().put("message", message)
        val response = patchJSON("customcommands/$guildId/$invoke", json)

        return parseTripleResponse(response)
    }

    fun deleteCustomCommand(guildId: Long, invoke: String): Boolean {
        val request = defaultRequest("customcommands/$guildId/$invoke").delete()

        return executeRequest(request).getBoolean("success")
    }

    private fun paginateData(path: String): JSONArray {
        val page1 = executeRequest(defaultRequest("$path?page=1")).getJSONObject("data")

        val data = page1.getJSONArray("data")

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

    private fun patchJSON(path: String, json: JSONObject): JSONObject {
        val body = RequestBody.create(null, json.toString())
        val request = defaultRequest(path).patch(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return executeRequest(request)
    }

    private fun postJSON(path: String, json: JSONObject): JSONObject {
        val body = RequestBody.create(null, json.toString())
        val request = defaultRequest(path).post(body).addHeader("Content-Type", APPLICATION_JSON.type)

        return executeRequest(request)
    }

    private fun executeRequest(request: Request.Builder): JSONObject {
        return WebUtils.ins.prepareRaw(request.build(), WebUtilsErrorUtils::toJSONObject).execute()
    }

    private fun defaultRequest(path: String): Request.Builder {
        return WebUtils.defaultRequest()
//            .url("https://apis.duncte123.me/bot/$path")
            .url("http://duncte123-apis-lumen.local/bot/$path")
            .get()
            .addHeader("Authorization", apiKey)
    }
}