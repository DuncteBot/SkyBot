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
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.web.WebUtils.EncodingType.APPLICATION_JSON
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.utils.AirUtils.colorToHex
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.routes.*
import ml.duncte123.skybot.web.routes.api.GetUserGuilds
import ml.duncte123.skybot.web.routes.api.Kpop
import ml.duncte123.skybot.web.routes.api.MainApi
import ml.duncte123.skybot.web.routes.crons.CronJobs
import ml.duncte123.skybot.web.routes.dashboard.*
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import org.apache.http.NameValuePair
import org.json.JSONObject
import spark.ModelAndView
import spark.Request
import spark.kotlin.*
import spark.template.jtwig.JtwigTemplateEngine
import java.util.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WebHolder(val shardManager: ShardManager, val variables: Variables) {

    val config = variables.config
    val commandManager = variables.commandManager
    val database = variables.database
    val audioUtils = variables.audioUtils

    val helpers = ApiHelpers()
    val engine = JtwigTemplateEngine("views")
    val oAuth2Client = OAuth2Client.Builder()
        .setClientId(config.discord.oauth.clientId)
        .setClientSecret(config.discord.oauth.clientSecret)
        .build()

    val FLASH_MESSAGE = "FLASH_MESSAGE"
    val SESSION_ID = "sessionId"
    val USER_SESSION = "USER_SESSION"
    val SPLITTER = ":SKIRT:"


    init {
        //Port has to be 2000 because of the apache proxy on the vps
        port(2000)

        staticFiles.location("/public")

        // Main routes
        Home(this)
        Commands(this)
        Suggest(this)
        Callback(this)
        Invite()
        LiveServerCount(this)

        // Dashboard routes
        Dashbord(this)
        BasicSettings(this)
        ModerationSettings(this)
        CustomCommandSettings(this)
        MessageSettings(this)
        MusicSettings(this)

        // Api routes
        MainApi(this)
        GetUserGuilds(this)
        Kpop(this)

        // Cronjob routes
        CronJobs(this)

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

    fun toMap(pairs: List<NameValuePair>): Map<String, String> {
        val map = HashMap<String, String>()
        for (i in pairs.indices) {
            val pair = pairs[i]
            map[pair.name] = pair.value
        }
        return map
    }

    fun get(path: String, map: WebVariables, model: String, withGuildData: Boolean = false) {
        get(path, DEFAULT_ACCEPT, engine) {

            if (withGuildData) {
                val guild = getGuildFromRequest(request)
                if (guild != null) {
                    val tcs = guild.textChannelCache.filter {
                        it.guild.selfMember.hasPermission(it, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)
                    }.toList()
                    val goodRoles = guild.roles.filter {
                        guild.selfMember.roles[0].canInteract(it) && it.name != "@everyone" && it.name != "@here"
                    }.toList()

                    val colorRaw = EmbedUtils.getColorOrDefault(guild.idLong, Settings.defaultColour)

                    map.put("goodChannels", tcs)
                    map.put("goodRoles", goodRoles)
                    map.put("settings", GuildSettingsUtils.getGuild(guild, variables))
                    map.put("guild", guild)
                    map.put("guildColor", colorToHex(colorRaw))

                    val session = request.session()
                    val message: String? = session.attribute(FLASH_MESSAGE)
                    if (!message.isNullOrEmpty()) {
                        session.attribute(FLASH_MESSAGE, null)
                        map.put("message", message)
                    } else {
                        map.put("message", false)
                    }
                }
            }

            map.put("color", colorToHex(Settings.defaultColour))
            ModelAndView(map.map, model)
        }
    }

    fun getGuildFromRequest(request: Request): Guild? {

        val guildId = request.params(":guildid")

        return shardManager.getGuildById(guildId) ?: null
    }

    fun getSession(request: Request): Session? {
        val session: String? = request.session().attribute(SESSION_ID)

        if (session.isNullOrEmpty()) {
            return null
        }

        return oAuth2Client.sessionController.getSession(session)
    }

    fun paramToBoolean(param: String?): Boolean {
        return if (param.isNullOrEmpty()) false else (param == "on")
    }
}
