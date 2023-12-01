package com.dunctebot.dashboard.websocket.handlers

import com.dunctebot.dashboard.websocket.handlers.base.SocketHandler
import com.fasterxml.jackson.databind.JsonNode

class PongHandler : SocketHandler() {
    override fun handleInternally(data: JsonNode?) {
        logger.debug("Got pong event")
    }
}
