/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import com.jagrosh.jdautilities.oauth2.Scope
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild
import me.duncte123.botCommons.web.WebUtils.EncodingType.APPLICATION_JSON
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.AirUtils.CONFIG
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import org.json.JSONObject
import spark.ModelAndView
import spark.Request
import spark.Response
import spark.Spark.path
import spark.kotlin.*
import spark.template.jtwig.JtwigTemplateEngine

class WebServer {

    private val engine = JtwigTemplateEngine("views")
    private val oAuth2Client = OAuth2Client.Builder()
            .setClientId(CONFIG.getLong("discord.oauth.clientId", 210363111729790977))
            .setClientSecret(CONFIG.getString("discord.oauth.clientSecret", "aaa"))
            .build()

    fun activate() {
        //Port has to be 2000 because of the apache proxy on the vps
        port(2000)

        staticFiles.location("/public")

        get("/", WebVariables().put("title", "Home"), "home.twig")

        get("/commands", WebVariables().put("title", "List of commands").put("prefix", Settings.PREFIX)
                .put("commands", AirUtils.COMMAND_MANAGER.sortedCommands), "commands.twig")


        path("/dashboard") {

            before("") {
                if (!request.session().attributes().contains("sessionId")) {
                    val url = oAuth2Client.generateAuthorizationURL(
                            CONFIG.getString("discord.oauth.redirUrl", "http://localhost:2000/callback"),
                            Scope.IDENTIFY, Scope.GUILDS
                    )
                    request.session(true).attribute("sessionId", "session_${System.currentTimeMillis()}")
                    response.redirect(url)
                }
            }

            get("", WebVariables().put("title", "Dashboard"), "dashboard.twig")
        }


        path("/server/:guildid") {

            before("*") {
                if (!request.session().attributes().contains("sessionId")) {
                    return@before response.redirect("/dashboard")
                }
                val guild = getGuildFromRequest(request, response)
                if (guild == null && !request.uri().contains("invalid")) {
                    return@before response.redirect("/server/${request.params(":guildid*")}/invalid")
                } else if (guild != null && request.uri().contains("invalid")) {
                    return@before response.redirect("/server/${request.params(":guildid*")}")
                }
            }

            //overview and editing
            get("", WebVariables()
                    .put("title", "Dashboard"), "serverSettings.twig", true)
            /*get("") {
                val guild = getGuildFromRequest(request, response)
                if (guild != null) {
                    val settings = GuildSettingsUtils.getGuild(guild)
                    return@get """<p>Guild prefix: ${settings.customPrefix}</p>
                    |<p>Join Message: ${settings.customJoinMessage}</p>
                    """.trimMargin()
                } else {
                }
            }*/
            //audio stuff
            get("/music") {
                val guild = getGuildFromRequest(request, response)
                if (guild != null) {
                    val mng = AudioUtils.ins.getMusicManager(guild, false)

                    if (mng != null) {
                        return@get """<p>Audio player details:</p>
                            |<p>Currently playing: <b>${if (mng.player.playingTrack != null) mng.player.playingTrack.info.title else "nothing"}</b></p>
                            |<p>Total tracks in queue: <b>${mng.scheduler.queue.size}</b></p>
                        """.trimMargin()
                    } else {
                        return@get "The audio player does not seem to be active"
                    }
                } else {
                }
            }

            //when the guild is not found
            get("/invalid") {
                response.status(404)
                "DuncteBot is not in the requested server, why don't you <a href=\"#\">invite it</a>?"
            }
        }

        get("/callback") {
            println(request.session().attribute("sessionId") as String)
            oAuth2Client.startSession(
                    request.queryParams("code"),
                    request.queryParams("state"),
                    request.session().attribute("sessionId")
            ).complete()
            response.redirect("/dashboard")
        }

        path("/api") {

            get("/getServerCount") {
                response.type(APPLICATION_JSON.type)
                return@get JSONObject()
                        .put("status", "success")
                        .put("server_count", SkyBot.getInstance().shardManager.guildCache.size())
                        .put("code", response.status())
            }

            get("/getUserGuilds") {
                response.type(APPLICATION_JSON.type)
                val guilds = ArrayList<JSONObject>()
                oAuth2Client.getGuilds(getSession(request)).complete().forEach {
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

        notFound {
            if(response.type() == APPLICATION_JSON.type) {
                return@notFound JSONObject()
                        .put("status", "failure")
                        .put("message", "'${request.pathInfo()}' was not found")
                        .put("code", response.status())
            } else {
                return@notFound engine.render(ModelAndView(WebVariables()
                        .put("title", "404").put("path", request.pathInfo()).map, "errors/404.twig"))
            }
        }

        internalServerError {
            if(response.type() == APPLICATION_JSON.type) {
                return@internalServerError JSONObject()
                        .put("status", "failure")
                        .put("message", "Internal server error")
                        .put("code", response.status())
            } else {
                return@internalServerError "<html><body><h1>Internal server error</h1></body></html>"
            }
        }
    }

    private fun get(path: String, map: WebVariables, model: String, withGuildData: Boolean = false) {
        get(path, DEFAULT_ACCEPT, engine) {
            if(withGuildData) {
                val guild = getGuildFromRequest(request, response)
                if(guild != null) {
                    val tcs = guild.textChannelCache.filter {
                        it.guild.selfMember.hasPermission(it, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)
                    }.toList()
                    val goodRoles = guild.roles.filter {
                        it.position < guild.selfMember.roles[0].position && it.name != "@everyone" && it.name != "@here"
                    }.toList()
                    map.put("goodChannels", tcs)
                    map.put("goodRoles", goodRoles)
                    map.put("settings", GuildSettingsUtils.getGuild(guild))
                    map.put("guild", guild)
                }
            }
            ModelAndView(map.map, model)
        }
    }

    private fun getGuildFromRequest(request: Request, response: Response): Guild? {

        val guildId = if (!request.params(":guildid").isNullOrEmpty())
            request.params(":guildid")
        else request.params(":guildid*")

        val guild = SkyBot.getInstance()
                .shardManager.getGuildById(guildId)

        if (guild == null) {
            response.body("DuncteBot is not in this server")
            return null
        }

        return guild
    }

    private fun getSession(request: Request) =
            oAuth2Client.sessionController.getSession(request.session().attribute("sessionId"))


    private fun guildToJson(guild: OAuth2Guild) = JSONObject()
            .put("name", guild.name)
            .put("iconId", guild.iconId)
            .put("iconUrl", guild.iconUrl)
            .put("owner", guild.isOwner)
            .put("id", guild.id)

}