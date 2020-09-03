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
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import gnu.trove.map.hash.TLongLongHashMap
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.web.ContentType
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.objects.guild.ProfanityFilterType
import ml.duncte123.skybot.objects.guild.WarnAction
import ml.duncte123.skybot.objects.web.ModelAndView
import ml.duncte123.skybot.objects.web.WebVariables
import ml.duncte123.skybot.utils.AirUtils.colorToHex
import ml.duncte123.skybot.utils.CommandUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.controllers.api.CustomCommands
import ml.duncte123.skybot.web.controllers.api.GetUserGuilds
import ml.duncte123.skybot.web.controllers.api.MainApi
import ml.duncte123.skybot.web.controllers.dashboard.BasicSettings
import ml.duncte123.skybot.web.controllers.dashboard.Dashboard
import ml.duncte123.skybot.web.controllers.dashboard.MessageSettings
import ml.duncte123.skybot.web.controllers.dashboard.ModerationSettings
import ml.duncte123.skybot.web.renderes.VelocityRenderer
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.sharding.ShardManager
import spark.Spark.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WebRouter(private val shardManager: ShardManager, private val variables: Variables) {
    private val config = variables.config
    private val mapper = variables.jackson

    private val engine = VelocityRenderer()
    private val oAuth2Client = OAuth2Client.Builder()
        .setClientId(config.discord.oauth.clientId)
        .setClientSecret(config.discord.oauth.clientSecret)
        .build()


    init {
        //Port has to be 2000 because of the proxy on the vps
        port(2000)

        if (Settings.IS_LOCAL) {
            val projectDir = System.getProperty("user.dir")
            val staticDir = "/src/main/resources/public"
            staticFiles.externalLocation(projectDir + staticDir)
        } else {
            staticFiles.location("/public")
        }

        val responseTransformer: (Any) -> String = {
            when (it) {
                is JsonNode -> {
                    mapper.writeValueAsString(it)
                }
                is ModelAndView -> {
                    engine.render(it)
                }
                else -> {
                    it.toString()
                }
            }
        }

        defaultResponseTransformer(responseTransformer)

        path("/server/$GUILD_ID") {
            before("/*") { request, response ->
                return@before Dashboard.beforeServer(request, response, shardManager)
            }

            get("/") { request, _ ->
                return@get Dashboard.serverSelection(request, shardManager)
            }

            // Basic settings
            getWithDefaultData("/basic", WebVariables().put("title", "Dashboard"),
                "dashboard/basicSettings.vm", true)

            post("/basic") { request, response ->
                return@post BasicSettings.save(request, response, shardManager, variables)
            }

            // Moderation settings
            getWithDefaultData("/moderation", WebVariables()
                .put("filterValues", ProfanityFilterType.values())
                .put("warnActionTypes", WarnAction.Type.values())
                .put("title", "Dashboard")
                .put("loggingTypes", GuildSettings.LOGGING_TYPES)
                .put("patronMaxWarnActions", WarnAction.PATRON_MAX_ACTIONS),
                "dashboard/moderationSettings.vm", true)

            post("/moderation") { request, response ->
                return@post ModerationSettings.save(request, response, shardManager, variables)
            }

            // Custom command settings
            getWithDefaultData("/customcommands", WebVariables().put("title", "Dashboard"),
                "dashboard/customCommandSettings.vm", true)

            // Message settings
            getWithDefaultData("/messages", WebVariables().put("title", "Dashboard"),
                "dashboard/welcomeLeaveDesc.vm", true)

            post("/messages") { request, response ->
                return@post MessageSettings.save(request, response, shardManager, variables)
            }

            /*get("/music") { request, _ ->
                val guild = WebHelpers.getGuildFromRequest(request, shardManager)
                    ?: return@get """{"message": "No guild? WOT"}"""
                val mng = variables.audioUtils.getMusicManager(guild)

                EarthUtils.gMMtoJSON(mng, variables.jackson)
            }*/
        }

        // Api routes
        path("/api") {
            before("/*") { _, response ->
                response.type(ContentType.JSON.type)
                response.header("Access-Control-Allow-Origin", "*")
                response.header("Access-Control-Allow-Credentials", "true")
                response.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH")
                response.header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, Authorization")
                response.header("Access-Control-Max-Age", "3600")
            }

            options("/*") { _, _ ->
                // Allow OPTIONS requests
            }

            get("/getServerCount") { _, response ->
                return@get MainApi.serverCount(response, shardManager, mapper)
            }

            get("/getUserGuilds") { request, response ->
                return@get GetUserGuilds.show(request, response, oAuth2Client, shardManager, mapper)
            }

            path("/customcommands/$GUILD_ID") {
                before("") { request, response ->
                    return@before CustomCommands.before(request, response, mapper)
                }

                get("") { request, response ->
                    return@get CustomCommands.show(request, response, shardManager, variables)
                }

                patch("") { request, response ->
                    return@patch CustomCommands.update(request, response, shardManager, variables)
                }

                post("") { request, response ->
                    return@post CustomCommands.create(request, response, shardManager, variables)
                }

                delete("") { request, response ->
                    return@delete CustomCommands.delete(request, response, shardManager, variables)
                }
            }
        }
    }

    private fun getWithDefaultData(path: String, map: WebVariables, viewName: String, withGuildData: Boolean = false) {
        get(path) { request, _ ->
            if (withGuildData) {
                val guild = request.getGuild(shardManager)

                if (guild != null) {
                    val guildId = guild.idLong

                    val tcs = guild.textChannelCache.filter {
                        it.guild.selfMember.hasPermission(it, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)
                    }.toList()

                    val goodRoles = guild.roleCache.filter {
                        guild.selfMember.canInteract(it) && it.name != "@everyone" && it.name != "@here"
                    }.filter { !it.isManaged }.toList()

                    val colorRaw = EmbedUtils.getColorOrDefault(guildId)
                    val currVcAutoRole = variables.vcAutoRoleCache.get(guildId)
                        ?: variables.vcAutoRoleCache.put(guildId, TLongLongHashMap())

                    map.put("goodChannels", tcs)
                    map.put("goodRoles", goodRoles)
                    map.put("voiceChannels", guild.voiceChannelCache)
                    map.put("currentVcAutoRole", currVcAutoRole)
                    map.put("settings", GuildSettingsUtils.getGuild(guildId, variables))
                    map.put("guild", guild)
                    map.put("guildColor", colorToHex(colorRaw))

                    map.put("guild_patron", CommandUtils.isGuildPatron(guild))

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

            map.toModelAndView(viewName)
        }
    }

    fun shutdown() {
        awaitStop()
    }

    companion object {
        const val FLASH_MESSAGE = "FLASH_MESSAGE"
        const val OLD_PAGE = "OLD_PAGE"
        const val SESSION_ID = "sessionId"
        const val USER_ID = "USER_SESSION"
        const val GUILD_ID = ":guildid"
        const val HOMEPAGE = "https://dunctebot.com/"
    }
}
