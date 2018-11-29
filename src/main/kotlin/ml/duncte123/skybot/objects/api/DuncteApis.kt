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
import me.duncte123.botcommons.web.WebUtilsErrorUtils
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject


class DuncteApis(private val apiKey: String) {

    fun getCustomCommands(): JSONArray {
        val page1 = executeRequest(defaultRequest("customcommands?page=1")).getJSONObject("data")

        var data = page1.getJSONArray("data")

        val totalPages = page1.getInt("last_page") + 1

        for (i in 2 until totalPages) {
            val page = executeRequest(defaultRequest("customcommands?page=$i")).getJSONObject("data")

            data = concatArray(data, page.getJSONArray("data"))
        }

        return data
    }

    fun deleteCustomCommand(guildId: Long, invoke: String): Boolean {
        val request = defaultRequest("customcommands/$guildId/$invoke").delete()

        return executeRequest(request).getBoolean("success")
    }

    private fun executeRequest(request: Request.Builder): JSONObject {
        return WebUtils.ins.prepareRaw(request.build(), WebUtilsErrorUtils::toJSONObject).execute()
    }

    private fun concatArray(vararg arrs: JSONArray): JSONArray {
        val result = JSONArray()

        for (arr in arrs) {
            for (i in 0 until arr.length()) {
                result.put(arr.get(i))
            }
        }

        return result
    }

    private fun defaultRequest(path: String): Request.Builder {
        return WebUtils.defaultRequest()
//            .url("https://apis.duncte123.me/$path")
            .url("http://duncte123-apis-lumen.local/bot/$path")
            .get()
            .addHeader("Authorization", apiKey)
    }
}
