package com.dunctebot.dashboard.rendering

class WebVariables {
    private var map = mutableMapOf<String, Any>()

    fun put(key: String, value: Any): WebVariables {
        map[key] = value
        return this
    }

    fun toMap(): Map<String, Any> {
        return map
    }
}
