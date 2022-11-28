package com.dunctebot.dashboard.websocket.handlers

import com.dunctebot.dashboard.websocket.handlers.base.SocketHandler
import com.fasterxml.jackson.databind.JsonNode

class FetchDataHandler : SocketHandler() {
    val waitingMap = mutableMapOf<String, (JsonNode) -> Unit>()

    override fun handleInternally(data: JsonNode?) {
        val identifier = data!!["identifier"].asText()

        if (waitingMap.containsKey(identifier)) {
            waitingMap[identifier]!!(data)

            waitingMap.remove(identifier)
        }
    }
}
