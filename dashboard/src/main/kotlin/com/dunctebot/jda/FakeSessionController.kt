package com.dunctebot.jda

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.utils.SessionController
import net.dv8tion.jda.internal.utils.tuple.Pair

class FakeSessionController : SessionController {
    private var globalRateLimit = 0L

    override fun appendSession(node: SessionController.SessionConnectNode) {
        // do nothing
    }

    override fun removeSession(node: SessionController.SessionConnectNode) {
        TODO("Not yet implemented")
    }

    override fun getGlobalRatelimit(): Long  = globalRateLimit

    override fun setGlobalRatelimit(ratelimit: Long) {
        globalRateLimit = ratelimit
    }

    override fun getGateway(api: JDA): String {
        TODO("Not yet implemented")
    }

    override fun getGatewayBot(api: JDA): Pair<String, Int> {
        TODO("Not yet implemented")
    }
}
