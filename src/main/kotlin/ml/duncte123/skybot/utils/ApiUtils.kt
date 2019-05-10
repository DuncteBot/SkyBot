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

package ml.duncte123.skybot.utils

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.adapters.DatabaseAdapter
import ml.duncte123.skybot.objects.api.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

@Author(nickname = "duncte123", author = "Duncan Sterken")
object ApiUtils {

    @JvmStatic
    fun getRandomLlama(): LlamaObject {
        val json = Variables.getInstance().apis
            .executeDefaultGetRequest("llama", false).get("data")

        return LlamaObject(json.get("id").asInt(), json.get("file").asText())
    }

    @JvmStatic
    fun getRandomAlpaca(callback: (AlpacaObject) -> Unit) {
        val json = Variables.getInstance().apis
            .executeDefaultGetRequest("alpaca", false).get("data")

        callback.invoke(AlpacaObject(json.get("file").asText()))
    }

    @JvmStatic
    fun getRandomSeal(callback: (String) -> Unit) {
        val json = Variables.getInstance().apis
            .executeDefaultGetRequest("seal", false).get("data")

        callback.invoke(json.get("file").asText())
    }

    @JvmStatic
    fun getRandomKpopMember(search: String = ""): KpopObject {
        val path = if (!search.isBlank()) "/${URLEncoder.encode(search, StandardCharsets.UTF_8)}" else ""


        val json = Variables.getInstance().apis
            .executeDefaultGetRequest("kpop$path", false).get("data")

        return KpopObject(
            json.get("id").asInt(),
            json.get("name").asText(),
            json.get("band").asText(),
            json.get("img").asText()
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
