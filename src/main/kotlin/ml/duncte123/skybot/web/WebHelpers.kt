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

package ml.duncte123.skybot.web

import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.session.Session
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Guild
import org.apache.http.NameValuePair
import spark.Request
import java.util.*

object WebHelpers {

    fun toMap(pairs: List<NameValuePair>): Map<String, String> {
        val map = HashMap<String, String>()
        for (i in pairs.indices) {
            val pair = pairs[i]
            map[pair.name] = pair.value
        }
        return map
    }

    fun getGuildFromRequest(request: Request, shardManager: ShardManager): Guild? {

        val guildId = request.params(":guildid")

        return shardManager.getGuildById(guildId) ?: null
    }

    fun paramToBoolean(param: String?): Boolean {
        return if (param.isNullOrEmpty()) false else (param == "on")
    }

    fun getSession(request: Request, oAuth2Client: OAuth2Client): Session? {
        val session: String? = request.session().attribute(WebRouter.SESSION_ID)

        if (session.isNullOrEmpty()) {
            return null
        }

        return oAuth2Client.sessionController.getSession(session)
    }
}
