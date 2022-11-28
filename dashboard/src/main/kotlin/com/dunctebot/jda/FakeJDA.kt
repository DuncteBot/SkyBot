package com.dunctebot.jda

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.internal.JDAImpl

class FakeJDA(private val restClient: JDARestClient, fakeClient: JDAImpl) : JDA by fakeClient {
    override fun retrieveUserById(id: Long, update: Boolean): RestAction<User> {
        return restClient.retrieveUserById(id.toString())
    }
}
