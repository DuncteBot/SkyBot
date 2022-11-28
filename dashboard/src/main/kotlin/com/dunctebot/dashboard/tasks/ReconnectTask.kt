package com.dunctebot.dashboard.tasks

import com.dunctebot.dashboard.websocket.WebsocketClient
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketState
import org.slf4j.LoggerFactory

class ReconnectTask(private val client: WebsocketClient): Runnable {
    private val logger = LoggerFactory.getLogger(ReconnectTask::class.java)

    override fun run() {
        try {
            val socket: WebSocket = client.socket
            if (!socket.isOpen && socket.state != WebSocketState.CONNECTING &&
                System.currentTimeMillis() - client.lastReconnectAttempt > client.reconnectInterval &&
                client.mayReconnect) {
                client.attemptReconnect()
            }
        } catch (e: Exception) {
            logger.error("Caught exception in reconnect thread", e)
        }
    }
}
