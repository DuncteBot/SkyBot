package com.dunctebot.dashboard.tasks

import com.dunctebot.dashboard.jsonMapper
import com.dunctebot.dashboard.websocket.WebsocketClient

class WSPingTask(private val client: WebsocketClient): Runnable {
    override fun run() {
        client.broadcast(
           jsonMapper.createObjectNode()
               .put("t", "PING")
        )
    }
}
