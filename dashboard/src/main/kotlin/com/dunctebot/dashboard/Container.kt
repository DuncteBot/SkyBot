package com.dunctebot.dashboard

import com.dunctebot.dashboard.websocket.WebsocketClient
import com.dunctebot.duncteapi.DuncteApi
import com.dunctebot.jda.JDARestClient
import com.fasterxml.jackson.databind.json.JsonMapper
import okhttp3.OkHttpClient

val restJDA = JDARestClient(System.getenv("BOT_TOKEN"))
val duncteApis = DuncteApi("Bot ${System.getenv("BOT_TOKEN")}")

val httpClient = OkHttpClient()
val jsonMapper = JsonMapper()
lateinit var webSocket: WebsocketClient

val server = WebServer()
