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
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.Permission
import spark.ModelAndView
import spark.Request
import spark.Response
import spark.template.jtwig.JtwigTemplateEngine

@Author(nickname = "duncte123", author = "Duncan Sterken")
object Dashboard {

    fun before(request: Request, response: Response, oAuth2Client: OAuth2Client, config: DunctebotConfig) {

        if (!request.session().attributes().contains(WebRouter.SESSION_ID)) {
            val url = oAuth2Client.generateAuthorizationURL(
                config.discord.oauth.redirUrl,
                Scope.IDENTIFY, Scope.GUILDS
            )
            request.session().attribute(WebRouter.SESSION_ID, "session_${System.currentTimeMillis()}")
            response.redirect(url)
        }
    }

    fun beforeServer(request: Request, response: Response, shardManager: ShardManager) {

        if (!request.session().attributes().contains(WebRouter.USER_SESSION)) {
            request.session().attribute(WebRouter.OLD_PAGE, request.pathInfo())
            return response.redirect("/")
        }

        val guild = WebHelpers.getGuildFromRequest(request, shardManager)
        if (guild == null && !request.uri().contains("invalid") && !request.uri().contains("noperms")) {
            return response.redirect("/server/${request.params(WebRouter.GUILD_ID)}/invalid")
        } else if (guild != null && request.uri().contains("invalid") && !request.uri().contains("noperms")) {
            return response.redirect("/server/${request.params(WebRouter.GUILD_ID)}/")
        }

        if (guild == null) {
            return
        }

        val userId = WebHelpers.getUserId(request)

        val user = shardManager.getUserById(userId)
        val member = guild.getMember(user)
        val hasPermission = member!!.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_SERVER)

        if (!hasPermission && !request.url().contains("noperms")) {
            return response.redirect("/server/${request.params(WebRouter.GUILD_ID)}/noperms")
        }
    }

    fun serverSelection(request: Request, shardManager: ShardManager, engine: JtwigTemplateEngine): Any {
        return engine.render(ModelAndView(WebVariables()
            .put("title", "Dashboard").put("id", request.params(WebRouter.GUILD_ID))
            .put("name", WebHelpers.getGuildFromRequest(request, shardManager)?.name).map,
            "dashboard/panelSelection.twig"))
    }

    fun postIssue(): Any {
        //                    val pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset())
//                    val params = toMap(pairs)
//
//                    val captcha = params["g-recaptcha-response"] + ""
//                    val name = params["name"]
//                    val description = params["desc"]
//                    // this should be an array
//                    val lastCommands = arrayListOf<String>()
//                    // full link or just invite code
//                    val invite = params["inv"]
//                    // hasteb.in or screenshot / video or whatever
//                    val detailed = params["detailed"]
//
//                    var i = 0
//                    var res: String? = params["cmds[$i]"]
//                    while (res != null) {
//                        lastCommands.add(res)
//                        res = params["cmds[${++i}]"]
//                    }
//
//                    if (name.isNullOrEmpty() || (description.isNullOrEmpty() || detailed.isNullOrEmpty())) {
//                        return@post renderSugPage(WebVariables().put("message", "Please fill in all the fields."))
//                    } else if (lastCommands.isEmpty()) {
//                        return@post renderSugPage(WebVariables().put("message", "Please add at least one command."))
//                    }
//
//                    val cap = helpers.verifyCapcha(captcha)
//
//                    if (!cap.getBoolean("success")) {
//                        return@post renderSugPage(WebVariables().put("message", "Captcha error: Please try again later"))
//                    }
//
//                    val json = JSONObject()
//                    json.put("description", "$description").put("inv", "$invite").put("detailedReport", "$detailed")
//                    val array = JSONArray()
//                    lastCommands.forEach { array.put(it) }
//                    return@post json.put("lastCommands", array).toString()
        return "{\"lastCommands\":[\"help\", \"join\", \"play duncan\"], \"detailedReport\":\"\",\"description\":\"dank meme\"," +
            "\"inv\":\"https://discord.gg/NKM9Xtk\"}"
    }

}
