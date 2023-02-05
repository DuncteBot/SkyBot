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

package ml.duncte123.skybot.web

import com.fasterxml.jackson.databind.JsonNode
import com.neovisionaries.ws.client.*
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.web.handlers.*
import ml.duncte123.skybot.web.tasks.WSPingTask
import ml.duncte123.skybot.websocket.ReconnectTask
import ml.duncte123.skybot.websocket.SocketHandler
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.IOUtil
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class WebSocketClient(
    private val variables: Variables,
    private val shardManager: ShardManager
) : WebSocketAdapter(), WebSocketListener {
    private val log = LoggerFactory.getLogger(WebSocketClient::class.java)
    private val executor = Executors.newSingleThreadExecutor {
        val t = Thread(it, "DB-SendThread")
        t.isDaemon = true
        return@newSingleThreadExecutor t
    }

    private val reconnectThread = Executors.newSingleThreadScheduledExecutor {
        val t = Thread(it, "DB-ReconnectThread")
        t.isDaemon = true
        return@newSingleThreadScheduledExecutor t
    }

    private val config = variables.config

    private val factory = WebSocketFactory()
        .setConnectionTimeout(5000)
        .setServerName(IOUtil.getHost(config.websocket.url))
    lateinit var socket: WebSocket
    private val handlersMap = mutableMapOf<String, SocketHandler>()

    var mayReconnect = true
    var lastReconnectAttempt = 0L
    var reconnectsAttempted = 0
    val reconnectInterval: Int
        get() = reconnectsAttempted * 2000 - 2000

    init {
        setupHandlers()
        connect()

        reconnectThread.scheduleWithFixedDelay(
            WSPingTask(this),
            1L,
            1L,
            TimeUnit.MINUTES
        )
        reconnectThread.scheduleWithFixedDelay(
            ReconnectTask(this),
            0L,
            500L,
            TimeUnit.MILLISECONDS
        )
    }

    override fun onConnected(websocket: WebSocket, headers: MutableMap<String, MutableList<String>>) {
        log.info("Connected to dashboard WebSocket")
    }

    override fun onDisconnected(
        websocket: WebSocket,
        serverCloseFrame: WebSocketFrame?,
        clientCloseFrame: WebSocketFrame?,
        closedByServer: Boolean
    ) {
        if (closedByServer && serverCloseFrame != null) {
            val reason = serverCloseFrame.closeReason ?: "<no reason given>"
            val code = serverCloseFrame.closeCode

            // TODO: keep trying to reconnect?
            if (code == 1000) {
                log.info(
                    "Connection to {} closed normally with reason {} (closed by server = true)",
                    websocket.uri,
                    reason
                )
            } else {
                log.info(
                    "Connection to {} closed abnormally with reason {}:{} (closed by server = true)",
                    websocket.uri,
                    code,
                    reason
                )
            }

            return
        } else if (clientCloseFrame != null) {
            val code = clientCloseFrame.closeCode
            val reason = clientCloseFrame.closeReason ?: "<no reason given>"

            // 1000 is normal
            if (code == 1000) {
                reconnectThread.shutdown()
                mayReconnect = false
            }

            log.info(
                "Connection to {} closed by client with code {} and reason {} (closed by server = false)",
                websocket.uri,
                code,
                reason
            )
        }
    }

    override fun onTextMessage(websocket: WebSocket, text: String) {
        val raw = variables.jackson.readTree(text)

        if (!raw.has("t")) {
            return
        }
        val type = raw["t"].asText()
        val handler = handlersMap[type]

        if (handler == null) {
            log.error("Unknown event or missing handler for type $type")
            return
        }

        handler.handle(raw)
    }

    override fun onError(websocket: WebSocket, cause: WebSocketException) {
        when (cause.cause) {
            is SocketTimeoutException -> {
                log.debug("Socket timed out")
            }
            is ConnectException -> {
                log.warn("Failed to connect to {}, retrying in {} seconds", websocket.uri, reconnectInterval / 1000)
            }
            is IOException -> {
                log.debug("Encountered I/O error", cause)
            }
            else -> {
                log.error("There was an error in the WebSocket connection", cause)
            }
        }
    }

    fun send(data: DataObject) {
        send(data.toString())
    }

    fun send(data: JsonNode) {
        send(variables.jackson.writeValueAsString(data))
    }

    private fun send(string: String) {
        executor.submit {
            try {
                socket.sendText(string)
            } catch (e: Exception) {
                log.error("Error while sending WS message", e)
            }
        }
    }

    override fun onThreadCreated(websocket: WebSocket, threadType: ThreadType, thread: Thread) {
        thread.name = "DuncteBotWS-$threadType"
    }

    fun shutdown() {
        mayReconnect = false
        socket.sendClose(WebSocketCloseCode.NORMAL)
        executor.shutdown()
        reconnectThread.shutdown()
    }

    private fun setupHandlers() {
        handlersMap[SocketTypes.DATA_UPDATE] = DataUpdateHandler(variables, this)
        handlersMap[SocketTypes.FETCH_DATA] = RequestHandler(variables, shardManager, this)
        handlersMap[SocketTypes.GUILD_SETTINGS] = GuildSettingsHandler(variables, this)
        handlersMap[SocketTypes.CUSTOM_COMMANDS] = CustomCommandHandler(variables, this)
        handlersMap[SocketTypes.PONG] = PongHandler(this)
    }

    fun attemptReconnect() {
        lastReconnectAttempt = System.currentTimeMillis()
        reconnectsAttempted++
        connect()
    }

    private fun connect() {
        if (this::socket.isInitialized && socket.isOpen) {
            socket.sendClose(WebSocketCloseCode.NORMAL)
        }

        socket = factory.createSocket(config.websocket.url)

        socket.setDirectTextMessage(false)
            .addHeader("X-DuncteBot", "bot")
            .addHeader("Accept-Encoding", "gzip")
            .addHeader("Authorization", variables.config.websocket.password)
            .addListener(this)

        try {
            socket.connect()
            // reset reconnects after successful connect
            reconnectsAttempted = 0
        } catch (e: Exception) {
            log.error("Failed to connect to WS, retrying in ${reconnectInterval / 1000} seconds", e)
        }
    }
}
