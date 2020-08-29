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

import com.neovisionaries.ws.client.*
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.config.DunctebotConfig
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.internal.utils.IOUtil
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.SocketTimeoutException

class WebSocketClient(
    private val variables: Variables, private val shardManager: ShardManager
): WebSocketAdapter(), WebSocketListener {
    private val log = LoggerFactory.getLogger(WebSocketClient::class.java)

    private val config = variables.config

    private val socket: WebSocket

    init {
        val factory = WebSocketFactory()
            .setConnectionTimeout(10000)
            .setServerName(IOUtil.getHost(config.websocket.url))

        socket = factory.createSocket(config.websocket.url)

        socket.setDirectTextMessage(true)
            .addHeader("Accept-Encoding", "gzip")
            .addHeader("Authorization", variables.apis.apiKey)
            .addListener(this)
            .connect()
    }

    override fun onConnected(websocket: WebSocket, headers: MutableMap<String, MutableList<String>>) {
        println(headers)
        // session is ready

        websocket.sendText("This is a test")
    }

    override fun onDisconnected(websocket: WebSocket, serverCloseFrame: WebSocketFrame, clientCloseFrame: WebSocketFrame, closedByServer: Boolean) {
        // Reconnect?
        println("disconnected")
    }

    override fun onTextMessage(websocket: WebSocket, text: String) {
        println("message: $text")
    }

    override fun onTextMessage(websocket: WebSocket, data: ByteArray) {
        val json = variables.jackson.readTree(data)

        println(json)
    }

    override fun onError(websocket: WebSocket, cause: WebSocketException) {
        when (cause.cause) {
            is SocketTimeoutException -> {
                log.debug("Socket timed out")
            }
            is IOException -> {
                log.debug("Encountered I/O error", cause)
            }
            else -> {
                log.error("There was an error in the WebSocket connection", cause)
            }
        }
    }

    override fun onThreadCreated(websocket: WebSocket, threadType: ThreadType, thread: Thread) {
        thread.name = "DuncteBotWS-$threadType"
    }

    fun shutdown() {
        //
    }
}
