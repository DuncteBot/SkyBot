package com.dunctebot.dashboard.websocket

import com.dunctebot.dashboard.duncteApis
import com.dunctebot.dashboard.jsonMapper
import com.dunctebot.dashboard.tasks.ReconnectTask
import com.dunctebot.dashboard.tasks.WSPingTask
import com.dunctebot.dashboard.utils.HashUtils
import com.dunctebot.dashboard.websocket.handlers.DataUpdateHandler
import com.dunctebot.dashboard.websocket.handlers.FetchDataHandler
import com.dunctebot.dashboard.websocket.handlers.PongHandler
import com.dunctebot.dashboard.websocket.handlers.RolesHashHandler
import com.dunctebot.dashboard.websocket.handlers.base.SocketHandler
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.neovisionaries.ws.client.*
import io.javalin.Javalin
import io.javalin.websocket.WsMessageContext
import net.dv8tion.jda.internal.utils.IOUtil
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class WebsocketClient(app: Javalin) : WebSocketAdapter(), WebSocketListener {
    private val logger = LoggerFactory.getLogger(WebsocketClient::class.java)
    val handlersMap = mutableMapOf<String, SocketHandler>()

    private val executor = Executors.newSingleThreadExecutor {
        val t = Thread(it, "WS-SendThread")
        t.isDaemon = true
        return@newSingleThreadExecutor t
    }

    private val reconnectThread = Executors.newSingleThreadScheduledExecutor {
        val t = Thread(it, "WS-ReconnectThread")
        t.isDaemon = true
        return@newSingleThreadScheduledExecutor t
    }

    private val factory = WebSocketFactory()
        .setConnectionTimeout(5000)
        .setServerName(IOUtil.getHost(System.getenv("WS_URL")))
    lateinit var socket: WebSocket

    var mayReconnect = true
    var lastReconnectAttempt = 0L
    var reconnectsAttempted = 0
    val reconnectInterval: Int
        get() = reconnectsAttempted * 2000 - 2000
    private var socketSendFn: (String) -> Unit = { socket.sendText(it) }

    init {
        setupHandlers()

        if (System.getenv("WS_URL") == "dash_is_server") {
            // init socket server
            val wsServer = WebsocketServer(app)

            socketSendFn = wsServer::broadcast
        } else {
            connect()

            reconnectThread.scheduleWithFixedDelay(
                ReconnectTask(this),
                0L,
                500L,
                TimeUnit.MILLISECONDS
            )

            reconnectThread.scheduleWithFixedDelay(
                WSPingTask(this),
                1L,
                1L,
                TimeUnit.MINUTES
            )
        }
    }

    override fun onConnected(websocket: WebSocket, headers: MutableMap<String, MutableList<String>>) {
        logger.info("Connected to WebSocket")
    }

    override fun onError(websocket: WebSocket, cause: WebSocketException) {
        logger.error("Error in websocket", cause)
    }

    override fun onDisconnected(websocket: WebSocket, serverCloseFrame: WebSocketFrame?, clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) {
        if (closedByServer && serverCloseFrame != null) {
            val reason = serverCloseFrame.closeReason ?: "<no reason given>"
            val code = serverCloseFrame.closeCode

            // TODO: keep trying to reconnect?
            if (code == 1000) {
                reconnectThread.shutdown()
                mayReconnect = false
                logger.info("Connection to {} closed normally with reason {} (closed by server = true)", websocket.uri, reason)
            } else {
                logger.info("Connection to {} closed abnormally with reason {}:{} (closed by server = true)", websocket.uri, code, reason)
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

            logger.info("Connection to {} closed by client with code {} and reason {} (closed by server = false)", websocket.uri, code, reason)
        }
    }

    override fun onTextMessage(websocket: WebSocket, text: String) {
        try {
            val raw = jsonMapper.readTree(text)

            logger.debug("<- {}", raw)

            if (!raw.has("t")) {
                return
            }

            val type = raw["t"].asText()
            val handler = handlersMap[type]

            if (handler == null) {
                logger.warn("Unknown event or missing handler for type $type")
                return
            }

            handler.handle(raw)
        } catch (e: IOException) {
            logger.error("Failed to parse json", e)
        }
    }

    override fun onThreadCreated(websocket: WebSocket, threadType: ThreadType, thread: Thread) {
        thread.name = "DuncteBotWS-$threadType"
    }

    fun requestData(data: JsonNode, callback: (JsonNode) -> Unit) {
        val hash = HashUtils.sha1(data.toString() + System.currentTimeMillis())

        (data as ObjectNode).put("identifier", hash)

        val request = jsonMapper.createObjectNode()
            .put("t", "FETCH_DATA")
            .set<ObjectNode>("d", data)

        (handlersMap["FETCH_DATA"]!! as FetchDataHandler).waitingMap[hash] = callback

        broadcast(request)
    }

    fun broadcast(message: JsonNode) {
        executor.submit {
            try {
                logger.debug("-> {}", message)

                socketSendFn(jsonMapper.writeValueAsString(message))
//                socket.sendText(jsonMapper.writeValueAsString(message))
            } catch (e: Exception) {
                logger.error("Error with broadcast", e)
            }
        }
    }

    private fun setupHandlers() {
        handlersMap["ROLES_PUT_HASH"] = RolesHashHandler()
        handlersMap["DATA_UPDATE"] = DataUpdateHandler()
        handlersMap["FETCH_DATA"] = FetchDataHandler()
        handlersMap["PONG"] = PongHandler()
    }

    fun attemptReconnect() {
        lastReconnectAttempt = System.currentTimeMillis()
        reconnectsAttempted++
        connect()
    }

    private fun connect() {
        socket = factory.createSocket(System.getenv("WS_URL"))

        socket.setDirectTextMessage(false) // decode to string
            .addHeader("X-DuncteBot", "dashboard")
            .addHeader("Accept-Encoding", "gzip")
            .addHeader("Authorization", duncteApis.apiKey)
            .addListener(this)

        try {
            socket.connect()
            // reset reconnects after successful connect
            reconnectsAttempted = 0
        } catch (e: Exception) {
            logger.error("Failed to connect to WS, retrying in ${reconnectInterval / 1000} seconds", e)
        }
    }
}
