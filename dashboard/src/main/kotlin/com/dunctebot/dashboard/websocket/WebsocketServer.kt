/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dunctebot.dashboard.websocket

import com.dunctebot.dashboard.jsonMapper
import com.dunctebot.dashboard.webSocket
import io.javalin.Javalin
import io.javalin.websocket.WsContext
import org.slf4j.LoggerFactory
import java.io.IOException

class WebsocketServer(app: Javalin) {
    private val logger = LoggerFactory.getLogger(WebsocketServer::class.java)
    private val bots = mutableListOf<WsContext>()

    init {
        app.ws("/socket") { config ->
            config.onConnect {  ctx ->
                val auth = ctx.header("Authorization")

                if (auth == null || auth != System.getenv("WS_SERVER_TOKEN")) {
                    ctx.closeSession(401, "Unauthorized")
                    return@onConnect
                }

                val botHeader = ctx.header("X-DuncteBot")

                if (botHeader == null || botHeader != "bot") {
                    ctx.closeSession(401, "Dashboards not supported")
                    return@onConnect
                }

                bots.add(ctx)
            }

            config.onClose { ctx ->
                bots.removeIf { it.sessionId == ctx.sessionId }
            }

            config.onMessage { ctx ->
                try {
                    val raw = jsonMapper.readTree(ctx.message())

                    logger.debug("<- {}", raw)

                    if (!raw.has("t")) {
                        return@onMessage
                    }

                    val type = raw["t"].asText()
                    val handler = webSocket.handlersMap[type]

                    if (handler == null) {
                        logger.warn("Unknown event or missing handler for type $type")
                        return@onMessage
                    }

                    handler.handle(raw)
                } catch (e: IOException) {
                    logger.error("Failed to parse json", e)
                }
            }
        }
    }

    fun broadcast(msg: String) {
        for (bot in bots) {
            bot.send(msg)
        }
    }
}
