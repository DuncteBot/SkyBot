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

package ml.duncte123.skybot.utils

import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.adapters.DatabaseAdapter
import ml.duncte123.skybot.extensions.illuminate
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.api.DuncteApis.Companion.API_HOST
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

@Author(nickname = "duncte123", author = "Duncan Sterken")
object ApiUtils {

    @JvmStatic
    fun getRandomLlama(): LlamaObject {
        val json = WebUtils.ins.getJSONObject("$API_HOST/llama").execute().getJSONObject("data")

        return LlamaObject(json.getInt("id"), json.getString("file").illuminate())
    }

    @JvmStatic
    fun getRandomAlpacaAsync(callback: (AlpacaObject) -> Unit) {
        WebUtils.ins.getJSONObject("$API_HOST/alpaca").async {
            callback.invoke(AlpacaObject(it.getString("data").illuminate()))
        }
    }

    @JvmStatic
    fun getRandomSealAsync(callback: (String) -> Unit) {
        WebUtils.ins.getJSONObject("$API_HOST/seal").async {
            callback.invoke(it.getString("data").illuminate())
        }
    }

    @JvmStatic
    fun getRandomKpopMember(search: String = ""): KpopObject {
        val path = if (!search.isBlank()) "/${URLEncoder.encode(search, StandardCharsets.UTF_8)}" else ""

        val json = WebUtils.ins.getJSONObject("$API_HOST/kpop$path").execute().getJSONObject("data")

        return KpopObject(
            json.getInt("id"),
            json.getString("name"),
            json.getString("band"),
            json.getString("img").illuminate()
        )
    }

    @JvmStatic
    fun getWarnsForUser(adapter: DatabaseAdapter, userId: Long, guildId: Long): WarnObject {

        val future = CompletableFuture<List<Warning>>()

        adapter.getWarningsForUser(userId, guildId) {
            future.complete(it)
        }

        return WarnObject(userId.toString(), future.get())
    }

}
