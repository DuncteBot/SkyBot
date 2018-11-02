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

package ml.duncte123.skybot.web.routes.api

import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.web.WebHolder
import net.dv8tion.jda.core.Permission
import org.json.JSONObject
import spark.kotlin.get
import java.util.ArrayList
import spark.Spark.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
class GetUserGuilds(private val holder: WebHolder) {

    init {
        path("/api") {
            get("/getUserGuilds") {

                if (!request.session().attributes().contains(holder.USER_SESSION)) {
                    return@get JSONObject()
                        .put("status", "error")
                        .put("message", "SESSION_INVALID")
                        .put("code", response.status())
                }

                val guilds = ArrayList<JSONObject>()
                holder.oAuth2Client.getGuilds(holder.getSession(request)).complete().forEach {
                    if (it.hasPermission(Permission.ADMINISTRATOR) || it.hasPermission(Permission.MANAGE_SERVER)) {
                        guilds.add(guildToJson(it))
                    }
                }
                return@get JSONObject()
                    .put("status", "success")
                    .put("guilds", guilds)
                    .put("code", response.status())
            }
        }
    }

    private fun guildToJson(guild: OAuth2Guild): JSONObject {

        val jdaGuild = holder.shardManager.getGuildById(guild.id)

        return JSONObject()
            .put("name", guild.name)
            .put("iconId", guild.iconId)
            .put("iconUrl", if (!guild.iconUrl.isNullOrEmpty()) guild.iconUrl
            else "https://cdn.discordapp.com/embed/avatars/0.png")
            .put("owner", guild.isOwner)
            .put("members", jdaGuild?.memberCache?.size() ?: false)
            .put("id", guild.id)
    }
}
