package com.dunctebot.dashboard

import com.dunctebot.dashboard.controllers.DashboardController
import com.dunctebot.dashboard.controllers.GuildController
import com.dunctebot.dashboard.controllers.RootController
import com.dunctebot.dashboard.controllers.api.*
import com.dunctebot.jda.oauth.OauthSessionController
import com.dunctebot.models.settings.GuildSetting
import com.dunctebot.models.settings.ProfanityFilterType
import com.dunctebot.models.settings.WarnAction
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.compression.CompressionStrategy
import io.javalin.http.ForbiddenResponse
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.rendering.vue.JavalinVue
import io.javalin.plugin.rendering.vue.VueComponent

class WebServer {
    private val app: Javalin
    val oAuth2Client: OAuth2Client = OAuth2Client.Builder()
        .setSessionController(OauthSessionController())
        .setClientId(System.getenv("OAUTH_CLIENT_ID").toLong())
        .setClientSecret(System.getenv("OAUTH_CLIENT_SECRET"))
        .build()

    init {
        val local = System.getenv("IS_LOCAL").toBoolean()

        this.app = Javalin.create { config ->
            config.compressionStrategy(CompressionStrategy.GZIP)
            config.autogenerateEtags = true
            config.showJavalinBanner = false
            config.enableWebjars()

            if (local) {
                val projectDir = System.getProperty("user.dir")
                val staticDir = "/src/main/resources/public"
                config.addStaticFiles(projectDir + staticDir, Location.EXTERNAL)
                config.enableDevLogging()
                JavalinVue.optimizeDependencies = false
                JavalinVue.rootDirectory {
                    it.externalPath("$projectDir/src/main/resources/vue") // live reloading :)
                }
            } else {
                config.addStaticFiles("/public", Location.CLASSPATH)
                JavalinVue.optimizeDependencies = true
            }
            // 191231307290771456
            // 191245668617158656

            // HACK: use a better solution.
            config.contextResolvers { resolvers ->
                resolvers.scheme = { _ -> if (local) "http" else "https" }
            }
        }

        // Non settings related routes
        this.app.get("roles/{hash}") { ctx -> GuildController.showGuildRoles(ctx) }

        this.app.get("register-server") { ctx ->
            VueComponent("one-guild-register", mapOf(
                "title" to "Register your server for patron perks",
                "captchaSitekey" to System.getenv("CAPTCHA_SITEKEY")
            )).handle(ctx)
        }

        addDashboardRoutes()
        addAPIRoutes()
        mapErrorRoutes()
    }

    private fun addDashboardRoutes() {
        this.app.get("callback") { ctx -> RootController.callback(ctx, oAuth2Client) }

        this.app.get("logout") { ctx ->
            ctx.req.session.invalidate()

            ctx.redirect("$HOMEPAGE?logout=true")
        }

        this.app.before("/") { ctx -> RootController.beforeRoot(ctx, oAuth2Client) }
        this.app.get("/", VueComponent("guilds"))

        this.app.routes {
            path("server") {
                before("$GUILD_ID*") { ctx -> DashboardController.before(ctx) }
                path(GUILD_ID) {
                    get("", VueComponent(
                        "settings",
                        // using the state here to provide items that are easily changed with package upates
                        mapOf(
                            // using id for type as that is what we will get back in the select
                            "filterValues" to ProfanityFilterType.values()
                                .map { mapOf("id" to it.type, "name" to it.getName()) },
                            "warnActionTypes" to WarnAction.Type.values()
                                .map { mapOf("id" to it.id, "name" to it.getName()) },
                            "loggingTypes" to GuildSetting.LOGGING_TYPES,
                            "patronMaxWarnActions" to WarnAction.PATRON_MAX_ACTIONS
                        )
                    ))
                }
            }
        }
    }

    private fun addAPIRoutes() {
        this.app.routes {
            path("api") {
                // This is just used by uptime robot to check if the application is up
                get("uptimerobot") { ctx ->
                    ctx.plainText()
                        .result("This is just used by uptime robot to check if the application is up")
                }

                post("update-data") { ctx -> DataController.updateData(ctx) }
                get("invalidate-tokens") { ctx -> DataController.invalidateTokens(ctx)  }

                path("check") {
                    post("user-guild") { ctx ->
                        GuildApiController.findUserAndGuild(ctx)
                    }
                }

                get("guilds") { ctx -> fetchGuildsOfUser(ctx, oAuth2Client) }
                post("guilds/patreon-settings") { ctx -> GuildController.handleOneGuildRegister(ctx) }

                // /api/guilds/{guild}/[settings|custom-commands|roles]
                path("guilds/$GUILD_ID") {
                    // we will use the custom command controller for now since this method protects all the settings routes
                    // before("*") { ctx -> CustomCommandController.before(ctx) }

                    path("settings") {
                        before("") { ctx -> CustomCommandController.before(ctx) }
                        get(::getSettings)
                        post(::postSettings)
                    }

                    get("roles") { ctx -> GuildController.guildRolesApiHandler(ctx) }

                    path("custom-commands") {
                        before("") { ctx -> CustomCommandController.before(ctx) }
                        get { ctx -> CustomCommandController.show(ctx) }
                        patch { ctx -> CustomCommandController.update(ctx) }
                        post { ctx -> CustomCommandController.create(ctx) }
                        delete { ctx -> CustomCommandController.delete(ctx) }
                    }
                }
            }
        }
    }

    private fun mapErrorRoutes() {
        this.app.error(404) { ctx ->
            VueComponent("error-404").handle(ctx)
        }

        this.app.error(500) { ctx ->
            VueComponent("error-500").handle(ctx)
        }

        // TODO: find a better solution, this is ugly (probably same thing with custom ex)
        this.app.exception(ForbiddenResponse::class.java) { ex, ctx ->
            //
        }
    }

    fun start() {
        if (System.getenv("IS_LOCAL").toBoolean()) {
            this.app.start(2000)
        } else {
            this.app.start(4567)
        }
    }

    fun shutdown() {
        this.app.stop()
    }

    companion object {
        const val FLASH_MESSAGE = "FLASH_MESSAGE"
        const val OLD_PAGE = "OLD_PAGE"
        const val SESSION_ID = "sessionId"
        const val USER_ID = "USER_SESSION"
        const val GUILD_ID = "{guildId}"
        const val HOMEPAGE = "https://www.duncte.bot/"
    }
}
