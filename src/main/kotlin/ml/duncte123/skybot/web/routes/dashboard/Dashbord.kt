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

package ml.duncte123.skybot.web.routes.dashboard

import com.jagrosh.jdautilities.oauth2.Scope
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.web.WebHolder
import net.dv8tion.jda.core.Permission
import org.json.JSONObject
import spark.ModelAndView
import spark.Spark.path
import spark.kotlin.before
import spark.kotlin.get
import spark.kotlin.post

@Author(nickname = "duncte123", author = "Duncan Sterken")
class Dashbord(private val holder: WebHolder) {

    init {
        path("/dashboard") {

            before("") {
                if (!request.session().attributes().contains(holder.SESSION_ID)) {
                    val url = holder.oAuth2Client.generateAuthorizationURL(
                        holder.config.discord.oauth.redirUrl,
                        Scope.IDENTIFY, Scope.GUILDS, Scope.GUILDS_JOIN
                    )
                    request.session(true).attribute(holder.SESSION_ID, "session_${System.currentTimeMillis()}")
                    response.redirect(url)
                }
            }

            holder.get("", WebVariables().put("title", "Dashboard"), "dashboard/index.twig")

            holder.get("/issue", WebVariables().put("title", "Issue Generator & Reporter"), "issues.twig")

            post("/issue") {
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
                return@post "{\"lastCommands\":[\"help\", \"join\", \"play duncan\"], \"detailedReport\":\"\",\"description\":\"dank meme\"," +
                    "\"inv\":\"https://discord.gg/NKM9Xtk\"}"
            }
        }

        get("/my-guild-count") {
            if (!request.session().attributes().contains(holder.USER_SESSION)) {
                return@get response.redirect("/dashboard")
            }

            val session = holder.getSession(request)
            val guilds = holder.oAuth2Client.getGuilds(session).complete()

            return@get JSONObject()
                .put("status", "success")
                .put("server_count", guilds.size)
                .put("code", response.status())
        }

        path("/server/:guildid") {

            before("/*") {
                if (!request.session().attributes().contains(holder.USER_SESSION)) {
                    return@before response.redirect("/dashboard")
                }

                val guild = holder.getGuildFromRequest(request)
                if (guild == null && !request.uri().contains("invalid") && !request.uri().contains("noperms")) {
                    return@before response.redirect("/server/${request.params(":guildid")}/invalid")
                } else if (guild != null && request.uri().contains("invalid") && !request.uri().contains("noperms")) {
                    return@before response.redirect("/server/${request.params(":guildid")}/")
                }

                val userId = (request.session().attribute(holder.USER_SESSION) as String).split(holder.SPLITTER)[1]

                val user = holder.shardManager.getUserById(userId)
                val member = guild?.getMember(user)
                val hasPermission = member!!.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_SERVER)

                if (!hasPermission && !request.url().contains("noperms")) {
                    return@before response.redirect("/server/${request.params(":guildid")}/noperms")
                }
            }

            get("/") {
                holder.engine.render(ModelAndView(WebVariables()
                    .put("title", "Dashboard").put("id", request.params(":guildid"))
                    .put("name", holder.getGuildFromRequest(request)?.name).map,
                    "dashboard/panelSelection.twig"))
            }

            //when the guild is not found
            get("/invalid") {
                response.status(404)
                "DuncteBot is not in the requested server, why don't you <a href=\"https://discordapp.com/oauth2" +
                    "/authorize?client_id=210363111729790977&guild_id=${request.params(":guildid")}&scope=bot&permissions=-1\" target=\"_blank\">invite it</a>?"
            }

            get("/noperms") {
                "<h1>You don't have permission to edit this server</h1>"
            }
        }
    }

}
