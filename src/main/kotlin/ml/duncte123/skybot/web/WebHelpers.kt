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

package ml.duncte123.skybot.web

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.session.Session
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.requests.FormRequestBody
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.sharding.ShardManager
import org.apache.http.client.utils.URLEncodedUtils
import spark.*
import java.nio.charset.StandardCharsets
import java.util.*

fun Request.getParamsMap() : Map<String, String> {
    val list = URLEncodedUtils.parse(this.body(), StandardCharsets.UTF_8)
    val map = HashMap<String, String>()
    list.forEach { pair ->
        /*if (pair.name.endsWith("[]") && !map.containsKey(pair.name)) {
            map[pair.name] = listOf<String>()
        }*/

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

    fun haltNotFound(request: Request, response: Response) {
        Spark.halt(404, CustomErrorPages.getFor(404, request, response) as String)
    }

    fun verifyCapcha(response: String, secret: String, mapper: ObjectMapper): JsonNode {
        val fields = FormRequestBody()
        fields.append("secret", secret)
        fields.append("response", response)
        val req = WebUtils.ins.postRequest("https://hcaptcha.com/siteverify", fields)
            .build({ WebParserUtils.toJSONObject(it, mapper) }, WebParserUtils::handleError)

        return req.execute()
    }
}
