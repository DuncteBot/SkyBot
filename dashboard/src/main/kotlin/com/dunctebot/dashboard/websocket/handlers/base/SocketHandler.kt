package com.dunctebot.dashboard.websocket.handlers.base

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class SocketHandler {
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun handle(raw: JsonNode) {
        try {
            handleInternally(raw["d"])
        } catch (e: Throwable) {
            logger.error("Unhandled exception in socket handler", e)
        }
    }

    protected abstract fun handleInternally(data: JsonNode?)
}
