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

package ml.duncte123.skybot.web

import com.fasterxml.jackson.databind.JsonNode
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import gnu.trove.map.hash.TLongLongHashMap
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.web.ContentType
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.utils.AirUtils.colorToHex
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.controllers.Callback
import ml.duncte123.skybot.web.controllers.OneGuildRegister
import ml.duncte123.skybot.web.controllers.api.*
import ml.duncte123.skybot.web.controllers.dashboard.BasicSettings
import ml.duncte123.skybot.web.controllers.dashboard.Dashboard
import ml.duncte123.skybot.web.controllers.dashboard.MessageSettings
import ml.duncte123.skybot.web.controllers.dashboard.ModerationSettings
import ml.duncte123.skybot.web.controllers.errors.HttpErrorHandlers
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.sharding.ShardManager
import spark.ModelAndView
import spark.Spark.*
import spark.template.jtwig.JtwigTemplateEngine

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WebRouter(private val shardManager: ShardManager, private val variables: Variables) {
    private val config = variables.config
    private val mapper = variables.jackson

    private val engine = JtwigTemplateEngine("views")
    private val oAuth2Client = OAuth2Client.Builder()
        .setClientId(config.discord.oauth.clientId)
        .setClientSecret(config.discord.oauth.clientSecret)
        .build()


    init {
        //Port has to be 2000 because of the apache proxy on the vps
        port(2000)

        staticFiles.location("/public")

        defaultResponseTransformer {
            if (it is JsonNode) {
                return@defaultResponseTransformer mapper.writeValueAsString(it)
            }

            return@defaultResponseTransformer it.toString()
        }

        get("/callback") { request, response ->
            return@get Callback.handle(request, response, oAuth2Client)
        }

        get("/logout") { request, response ->
            request.session().invalidate()

            return@get response.redirect(HOMEPAGE)
        }

        get("/invite") { _, response ->
            return@get response.redirect("https://lnk.dunctebot.com/invite")
        }

        get("/register-server", WebVariables()
            .put("title", "Register your server for patron perks")
            .put("chapta_sitekey", config.apis.chapta.sitekey), "oneGuildRegister.twig")

        post("/register-server") { request, _ ->
            return@post OneGuildRegister.post(request, shardManager, variables, engine, mapper)
        }

        path("/") {

            before("") { request, response ->
                return@before Dashboard.before(request, response, oAuth2Client, config)
            }

            get("", WebVariables().put("title", "Dashboard"), "dashboard/index.twig")
            get("/issue", WebVariables().put("title", "Issue Generator & Reporter"), "issues.twig")

            post("/issue") { _, _ ->
                return@post Dashboard.postIssue()
            }
        }

        path("/server/$GUILD_ID") {
            before("/*") { request, response ->
                return@before Dashboard.beforeServer(request, response, shardManager)
            }

            get("/") { request, _ ->
                return@get Dashboard.serverSelection(request, shardManager, engine)
            }

            get("/invalid") { request, response ->
                response.status(404)

                return@get "DuncteBot is not in the requested server, why don't you <a href=\"https://discordapp.com/oauth2" +
                    "/authorize?client_id=210363111729790977&guild_id=${request.params(GUILD_ID)}" +
                    "&scope=bot&permissions=-1\" target=\"_blank\">invite it</a>?"
            }

            get("/noperms") { _, _ ->
                return@get "<h1>You don't have permission to edit this server</h1>"
            }

            // Basic settings
            get("/basic", WebVariables().put("title", "Dashboard"),
                "dashboard/basicSettings.twig", true)

            post("/basic") { request, response ->
                return@post BasicSettings.save(request, response, shardManager, variables)
            }

            // Moderation settings
            get("/moderation", WebVariables().put("title", "Dashboard"),
                "dashboard/moderationSettings.twig", true)

            post("/moderation") { request, response ->
                return@post ModerationSettings.save(request, response, shardManager, variables)
            }

            // Custom command settings
            get("/customcommands", WebVariables().put("title", "Dashboard"),
                "dashboard/customCommandSettings.twig", true)

            // Message settings
            get("/messages", WebVariables().put("title", "Dashboard"),
                "dashboard/welcomeLeaveDesc.twig", true)

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

            get("/joinGuild") { _, response ->
                return@get MainApi.joinGuild(response)
            }

            get("/getUserGuilds") { request, response ->
                return@get GetUserGuilds.show(request, response, oAuth2Client, shardManager, mapper)
            }

            post("/suggest") { request, response ->
                return@post Suggest.create(request, response, config, mapper)
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

            post("/checkUserAndGuild") { request, response ->
                return@post FindUserAndGuild.get(request, response, shardManager, mapper)
            }

            get("/commands.json") { _, _ ->
                return@get CommandTransformers.toJson(variables.commandManager, mapper)
            }

            get("/commands.php") { _, response ->
                response.type(ContentType.TEXT_PLAIN.type)

                return@get CommandTransformers.toPHP(variables.commandManager)
            }
        }

        notFound { request, response ->
            return@notFound HttpErrorHandlers.notFound(request, response, engine, mapper)
        }

        internalServerError { request, response ->
            return@internalServerError HttpErrorHandlers.internalServerError(request, response, mapper)
        }
    }

    fun get(path: String, map: WebVariables, model: String, withGuildData: Boolean = false) {
        get(path) { request, _ ->
            if (withGuildData) {
                val guild = WebHelpers.getGuildFromRequest(request, shardManager)

                if (guild != null) {
                    val tcs = guild.textChannelCache.filter {
                        it.guild.selfMember.hasPermission(it, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)
                    }.toList()

                    val goodRoles = guild.roleCache.filter {
                        guild.selfMember.canInteract(it) && it.name != "@everyone" && it.name != "@here"
                    }.filter { !it.isManaged }.toList()

                    val colorRaw = EmbedUtils.getColorOrDefault(guild.idLong, Settings.DEFAULT_COLOUR)
                    val currVcAutoRole = variables.vcAutoRoleCache.get(guild.idLong)
                        ?: variables.vcAutoRoleCache.put(guild.idLong, TLongLongHashMap())

                    map.put("goodChannels", tcs)
                    map.put("goodRoles", goodRoles)
                    map.put("voiceChannels", guild.voiceChannelCache)
                    map.put("currentVcAutoRole", currVcAutoRole)
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

            map.put("color", colorToHex(Settings.DEFAULT_COLOUR))

            engine.render(ModelAndView(map.map, model))
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
