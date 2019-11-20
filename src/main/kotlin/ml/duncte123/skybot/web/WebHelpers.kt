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

package ml.duncte123.skybot.web

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.session.Session
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.requests.EmptyFromRequestBody
import me.duncte123.botcommons.web.requests.FormRequestBody
import me.duncte123.weebJava.helpers.QueryBuilder
import ml.duncte123.skybot.objects.config.DunctebotConfig
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.sharding.ShardManager
import org.apache.http.client.utils.URLEncodedUtils
import spark.Request
import java.nio.charset.StandardCharsets
import java.util.*

fun Request.getParamsMap() : Map<String, String> {
    val list = URLEncodedUtils.parse(this.body(), StandardCharsets.UTF_8)
    val map = HashMap<String, String>()
    for (i in list.indices) {
        val pair = list[i]
        map[pair.name] = pair.value
    }
    return map
}

fun Request.getUserId(): String = this.session().attribute(WebRouter.USER_ID) as String

fun Request.getGuild(shardManager: ShardManager): Guild? = shardManager.getGuildById(this.params(WebRouter.GUILD_ID))

fun Request.getSession(oAuth2Client: OAuth2Client): Session? {
    val session: String? = this.session().attribute(WebRouter.SESSION_ID)

    if (session.isNullOrEmpty()) {
        return null
    }

    return oAuth2Client.sessionController.getSession(session)
}

fun String?.toCBBool(): Boolean = if (this.isNullOrEmpty()) false else (this == "on")

object WebHelpers {

    fun verifyCapcha(response: String, secret: String, mapper: ObjectMapper): JsonNode {
        val fields = FormRequestBody()
        fields.append("secret", secret)
        fields.append("response", response)
        val req = WebUtils.ins.postRequest("https://www.google.com/recaptcha/api/siteverify", fields)
            .build({ WebParserUtils.toJSONObject(it, mapper) }, WebParserUtils::handleError)

        return req.execute()
    }

    fun addTrelloCard(name: String, desc: String, config: DunctebotConfig.Apis.Trello, mapper: ObjectMapper): JsonNode {
        val query = QueryBuilder()
            .append("https://api.trello.com/1/cards")
            .append("name", name)
            .append("desc", desc)
            .append("pos", "bottom")
            .append("idList", "5ad2a228bef59be0aca289c9")
            .append("keepFromSource", "all")
            .append("key", config.key)
            .append("token", config.token)

        return WebUtils.ins.postRequest(query.build(), EmptyFromRequestBody())
            .build({ WebParserUtils.toJSONObject(it, mapper) }, WebParserUtils::handleError)
            .execute()
    }
}
