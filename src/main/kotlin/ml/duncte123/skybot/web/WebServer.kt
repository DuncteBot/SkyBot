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
import ml.duncte123.skybot.utils.AirUtils.colorToHex
import ml.duncte123.skybot.utils.ApiUtils
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.json.JSONObject
import spark.ModelAndView
import spark.Request
import spark.Spark.path
import spark.kotlin.*
import spark.template.jtwig.JtwigTemplateEngine
import java.nio.charset.Charset
import java.sql.SQLException
import java.util.*


class WebServer {

    private val helpers = ApiHelpers()
    private val engine = JtwigTemplateEngine("views")
    private val oAuth2Client = OAuth2Client.Builder()
            .setClientId(CONFIG.getLong("discord.oauth.clientId", 210363111729790977))
            .setClientSecret(CONFIG.getString("discord.oauth.clientSecret", "aaa"))
            .build()

    init {
        if(!AirUtils.CONFIG.getBoolean("discord.local", false)) {

            //Port has to be 2000 because of the apache proxy on the vps
            port(2000)

            staticFiles.location("/public")

            get("/", WebVariables().put("title", "Home"), "home.twig")

            get("/commands", WebVariables().put("title", "List of commands").put("prefix", Settings.PREFIX)
                    .put("commands", AirUtils.COMMAND_MANAGER.sortedCommands), "commands.twig")

            get("/suggest", WebVariables().put("title", "Leave a suggestion")
                    .put("chapta_sitekey", AirUtils.CONFIG.getString("apis.chapta.sitekey")), "suggest.twig")

            post("/suggest") {
                val pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset())
                val params = toMap(pairs)

                val captcha = params["g-recaptcha-response"] + ""
                val name = params["name"]
                val suggestion = params["sug"]
                val description = params["desc"]

                if (name.isNullOrEmpty() || suggestion.isNullOrEmpty()) {
                    return@post renderSugPage(WebVariables().put("message", "Please fill in all the fields."))
                }

                val cap = helpers.verifyCapcha(captcha)

                if (!cap.getBoolean("success")) {
                    return@post renderSugPage(WebVariables().put("message", "Captcha error: Please try again later"))
                }

                val extraDesc = if (!description.isNullOrEmpty()) "$description\n\n" else ""
                val descText = "${extraDesc}Suggested by: $name\nSuggested from website"

                val url = helpers.addTrelloCard(suggestion.toString(), descText).getString("shortUrl")

                renderSugPage(WebVariables().put("message", "Thanks for submitting, you can view your suggestion <a target='_blank' href='$url'>here</a>"))
            }


            path("/dashboard") {

                before("") {
                    if (!request.session().attributes().contains("sessionId")) {
                        val url = oAuth2Client.generateAuthorizationURL(
                                CONFIG.getString("discord.oauth.redirUrl", "http://localhost:2000/callback"),
                                Scope.IDENTIFY, Scope.GUILDS, Scope.GUILDS_JOIN
                        )
                        request.session(true).attribute("sessionId", "session_${System.currentTimeMillis()}")
                        response.redirect(url)
                    }
                }

                get("", WebVariables().put("title", "Dashboard"), "dashboard.twig")

                get("/issue", WebVariables().put("title", "Issue Generator & Reporter"), "issues.twig")

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


            path("/server/:guildid") {

                before("/*") {
                    if (!request.session().attributes().contains("sessionId")) {
                        return@before response.redirect("/dashboard")
                    }
                    val guild = getGuildFromRequest(request)
                    if (guild == null && !request.uri().contains("invalid")) {
                        return@before response.redirect("/server/${request.params(":guildid")}/invalid")
                    } else if (guild != null && request.uri().contains("invalid")) {
                        return@before response.redirect("/server/${request.params(":guildid")}/")
                    }
                }

                //overview and editing
                get("/", WebVariables()
                        .put("title", "Dashboard"), "serverSettings.twig", true)

                post("/") {
                    val pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset())
                    val params = toMap(pairs)

                    val prefix = params["prefix"]
                    val serverDescription = params["serverDescription"]
                    val welcomeChannel = params["welcomeChannel"]
                    val welcomeLeaveEnabled = paramToBoolean(params["welcomeChannelCB"])
                    val autorole = params["autoRoleRole"]
                    //val autoRoleEnabled      = params["autoRoleRoleCB"]
                    val modLogChannel = params["modChannel"]
                    val announceTracks = paramToBoolean(params["announceTracks"])
                    val autoDeHoist = paramToBoolean(params["autoDeHoist"])
                    val filterInvites = paramToBoolean(params["filterInvites"])
                    val swearFilter = paramToBoolean(params["swearFilter"])
                    val welcomeMessage = params["welcomeMessage"]
                    val leaveMessage = params["leaveMessage"]
                    val muteRole = params["muteRole"]
                    val spamFilter = paramToBoolean(params["spamFilter"])
                    val kickMode = paramToBoolean(params["kickMode"])

                    val rateLimits = LongArray(6)

                    for (i in 0..5) {
                        rateLimits[i] = params["rateLimits[$i]"]!!.toLong()
                    }

                    val guild = getGuildFromRequest(request)

                    val newSettings = GuildSettingsUtils.getGuild(guild)
                            .setCustomPrefix(prefix)
                            .setServerDesc(serverDescription)
                            .setWelcomeLeaveChannel(welcomeChannel)
                            .setCustomJoinMessage(welcomeMessage)
                            .setCustomLeaveMessage(leaveMessage)
                            .setEnableJoinMessage(welcomeLeaveEnabled)
                            .setAutoroleRole(autorole)
                            .setLogChannel(modLogChannel)
                            .setAnnounceTracks(announceTracks)
                            .setAutoDeHoist(autoDeHoist)
                            .setFilterInvites(filterInvites)
                            .setMuteRoleId(muteRole)
                            .setKickState(kickMode)
                            .setRatelimits(rateLimits)
                            .setEnableSpamFilter(spamFilter)
                            .setEnableSwearFilter(swearFilter)

                    GuildSettingsUtils.updateGuildSettings(guild, newSettings)

                    response.redirect(request.url() + "?message=<h4>Settings updated</h4>")
                }

                //audio stuff
                get("/music") {
                    val guild = getGuildFromRequest(request)
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
                        return@get "ERROR"
                    }
                }

                //when the guild is not found
                get("/invalid") {
                    response.status(404)
                    "DuncteBot is not in the requested server, why don't you <a href=\"https://discordapp.com/oauth2" +
                            "/authorize?client_id=210363111729790977&guild_id=${request.params(":guildid")}&scope=bot&permissions=-1\" target=\"_blank\">invite it</a>?"
                }
            }

            get("/callback") {
                oAuth2Client.startSession(
                        request.queryParams("code"),
                        request.queryParams("state"),
                        request.session().attribute("sessionId")
                ).complete()
                response.redirect("/dashboard")
            }

            get("/liveServerCount") {
                engine.render(ModelAndView(mapOf("nothing" to "something"),
                        "static/liveServerCount.twig"))
            }

            path("/api") {

                before("/*") {
                    response.type(APPLICATION_JSON.type)
                }

                get("/getServerCount") {
                    return@get JSONObject()
                            .put("status", "success")
                            .put("server_count", SkyBot.getInstance().shardManager.guildCache.size())
                            .put("code", response.status())
                }

                get("/getUserGuilds") {
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

                get("/joinGuild") {
                    try {
                        val session = getSession(request)
                        SkyBot.getInstance().shardManager.getGuildById("191245668617158656")
                                .addMember(session.accessToken, oAuth2Client.getUser(session).complete().id).complete()
                        response.redirect("/dashboard")
                    } catch (e: IllegalStateException) {
                        response.redirect("https://discord.gg/NKM9Xtk")
                    }
                }

                get("/llama") {
                    return@get ApiUtils.getRandomLlama().toJson()
                            .put("status", "success")
                            .put("code", response.status())
                }

                get("/kpop") {
                    val search = request.queryParamOrDefault("search", "")
                    try {
                        return@get ApiUtils.getRandomKpopMember(search).toJson()
                                .put("status", "success")
                                .put("code", response.status())
                    } catch (e: SQLException) {
                        response.status(404)
                        return@get JSONObject()
                                .put("status", "faiure")
                                .put("message", "Nothing found")
                                .put("code", response.status())
                    }
                }
            }

            path("/crons") {

                get("/clearExpiredWarns") {
                    AirUtils.DB.connManager.connection.createStatement()
                            .execute("DELETE FROM `warnings` WHERE (CURDATE() >= DATE_ADD(expire_date, INTERVAL 5 DAY))")
                }

            }

            notFound {
                if (request.headers("Accept") == APPLICATION_JSON.type || response.type() == APPLICATION_JSON.type) {
                    response.type(APPLICATION_JSON.type)
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
                if (request.headers("Accept") == APPLICATION_JSON.type || response.type() == APPLICATION_JSON.type) {
                    response.type(APPLICATION_JSON.type)
                    return@internalServerError JSONObject()
                            .put("status", "failure")
                            .put("message", "Internal server error")
                            .put("code", response.status())
                } else {
                    return@internalServerError "<html><body><h1>Internal server error</h1></body></html>"
                }
            }
        }
    }

    private fun get(path: String, map: WebVariables, model: String, withGuildData: Boolean = false) {
        get(path, DEFAULT_ACCEPT, engine) {
            if (withGuildData) {
                val guild = getGuildFromRequest(request)
                if (guild != null) {
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

                    if(queryMap().hasKey("message"))
                        map.put("message", queryParams("message"))
                }
            }
            map.put("color", colorToHex(Settings.defaultColour))
            ModelAndView(map.map, model)
        }
    }

    private fun renderSugPage(map: WebVariables): String {
        map.put("title", "Leave a suggestion")
                .put("chapta_sitekey", AirUtils.CONFIG.getString("apis.chapta.sitekey"))

        return engine.render(ModelAndView(map.map, "suggest.twig"))
    }

    private fun getGuildFromRequest(request: Request): Guild? {

        val guildId = request.params(":guildid")

        return SkyBot.getInstance()
                .shardManager.getGuildById(guildId) ?: null
    }

    private fun getSession(request: Request) =
            oAuth2Client.sessionController.getSession(request.session().attribute("sessionId"))


    private fun guildToJson(guild: OAuth2Guild):JSONObject {

        val jdaGuild = SkyBot.getInstance().shardManager.getGuildById(guild.id)

        return JSONObject()
                .put("name", guild.name)
                .put("iconId", guild.iconId)
                .put("iconUrl", if (!guild.iconUrl.isNullOrEmpty()) guild.iconUrl
                else "https://cdn.discordapp.com/embed/avatars/0.png")
                .put("owner", guild.isOwner)
                .put("members", jdaGuild?.memberCache?.size() ?: false)
                .put("id", guild.id)
    }

    private fun toMap(pairs: List<NameValuePair>): Map<String, String> {
        val map = HashMap<String, String>()
        for (i in pairs.indices) {
            val pair = pairs[i]
            map[pair.name] = pair.value
        }
        return map
    }

    private fun paramToBoolean(param: String?): Boolean {
        return if (param.isNullOrEmpty()) false else (param == "on")
    }

}