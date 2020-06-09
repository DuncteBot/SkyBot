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

package ml.duncte123.skybot.web.controllers.dashboard

import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.Scope
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.objects.config.DunctebotConfig
import ml.duncte123.skybot.web.WebRouter
import ml.duncte123.skybot.web.getGuild
import ml.duncte123.skybot.web.getUserId
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.sharding.ShardManager
import spark.Request
import spark.Response

@Author(nickname = "duncte123", author = "Duncan Sterken")
object Dashboard {

    fun before(request: Request, response: Response, oAuth2Client: OAuth2Client, config: DunctebotConfig) {
        val ses = request.session()

        if (ses.attribute<String?>(WebRouter.SESSION_ID) == null) {
            val url = oAuth2Client.generateAuthorizationURL(
                config.discord.oauth.redirUrl,
                Scope.IDENTIFY, Scope.GUILDS
            )

            ses.attribute(WebRouter.SESSION_ID, "session_${System.currentTimeMillis()}")

            return response.redirect("$url&prompt=none")
//            response.redirect(url)
        }
    }

    fun beforeServer(request: Request, response: Response, shardManager: ShardManager) {
        val ses = request.session()

        if (ses.attribute<String?>(WebRouter.USER_ID) == null || ses.attribute<String?>(WebRouter.SESSION_ID) == null) {
            request.session().attribute(WebRouter.OLD_PAGE, request.pathInfo())
            return response.redirect("/")
        }

        val guild = request.getGuild(shardManager)
        val guildId = request.params(WebRouter.GUILD_ID)

        if (guild == null && !request.uri().contains("invalid") && !request.uri().contains("noperms")) {
            return response.redirect("/server/${guildId}/invalid")
        }

        if (guild != null && request.uri().contains("invalid") && !request.uri().contains("noperms")) {
            return response.redirect("/server/${guildId}/")
        }

        // Because this method gets called before every /server/... we have to return on null guilds
        // because it gets here before it gets to /server/.../invalid
        if (guild == null) {
            return
        }

        val userId = request.getUserId()
        val member = try {
            guild.retrieveMemberById(userId).complete()
        } catch (e: ErrorResponseException) {
            return response.redirect("/server/${guildId}/noperms")
        }

        if (!member.hasPermission(Permission.MANAGE_SERVER) && !request.url().contains("noperms")) {
            return response.redirect("/server/${guildId}/noperms")
        }
    }

    fun serverSelection(request: Request, shardManager: ShardManager): Any {
        return WebVariables()
            .put("title", "Dashboard")
            .put("id", request.params(WebRouter.GUILD_ID))
            .put("name", request.getGuild(shardManager)?.name)
            .toModelAndView("dashboard/panelSelection.vm")
    }
}
