package com.dunctebot.dashboard.controllers.api

import com.dunctebot.dashboard.*
import com.fasterxml.jackson.databind.node.ObjectNode
import io.javalin.http.Context

object DataController {
    fun updateData(ctx: Context) {
        ctx.authOrFail()

        // parse the data to make sure that it is proper json
        val updateData = ctx.jsonBody
        val wsRequest = jsonMapper.createObjectNode()
            .put("t", "DATA_UPDATE")
            .set<ObjectNode>("d", updateData)

        // send the data to all instances that are connected
        webSocket.broadcast(wsRequest)

        ctx.json(
            jsonMapper.createObjectNode().put("success", true)
        )
    }

    fun invalidateTokens(ctx: Context) {
        ctx.authOrFail()

        duncteApis.validTokens.clear()

        ctx.result("ok")
    }
}
