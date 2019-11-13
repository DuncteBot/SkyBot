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

package ml.duncte123.skybot.web.controllers.dashboard

import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.Scope
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.objects.config.DunctebotConfig
import ml.duncte123.skybot.web.WebHelpers
import ml.duncte123.skybot.web.WebRouter
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.sharding.ShardManager
import spark.ModelAndView
import spark.Request
import spark.Response
import spark.template.jtwig.JtwigTemplateEngine

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

            response.redirect("$url&prompt=none")
//            response.redirect(url)
        }
    }

    fun beforeServer(request: Request, response: Response, shardManager: ShardManager) {
        val ses = request.session()

        if (ses.attribute<String?>(WebRouter.USER_ID) == null || ses.attribute<String?>(WebRouter.SESSION_ID) == null) {
            request.session().attribute(WebRouter.OLD_PAGE, request.pathInfo())
            return response.redirect("/")
        }

        val guild = WebHelpers.getGuildFromRequest(request, shardManager)

        if (guild == null && !request.uri().contains("invalid") && !request.uri().contains("noperms")) {
            return response.redirect("/server/${request.params(WebRouter.GUILD_ID)}/invalid")
        }

        if (guild != null && request.uri().contains("invalid") && !request.uri().contains("noperms")) {
            return response.redirect("/server/${request.params(WebRouter.GUILD_ID)}/")
        }

        if (guild == null) {
            return
        }

        val userId = WebHelpers.getUserId(request)
        val member = guild.getMemberById(userId)
                ?: return response.redirect("/server/${request.params(WebRouter.GUILD_ID)}/noperms")

        if (!member.hasPermission(Permission.MANAGE_SERVER) && !request.url().contains("noperms")) {
            return response.redirect("/server/${request.params(WebRouter.GUILD_ID)}/noperms")
        }
    }

    fun serverSelection(request: Request, shardManager: ShardManager, engine: JtwigTemplateEngine): Any {
        return engine.render(ModelAndView(WebVariables()
            .put("title", "Dashboard").put("id", request.params(WebRouter.GUILD_ID))
            .put("name", WebHelpers.getGuildFromRequest(request, shardManager)?.name).map,
            "dashboard/panelSelection.twig"))
    }
}
