package com.dunctebot.jda.oauth

import com.jagrosh.jdautilities.oauth2.session.SessionController
import com.jagrosh.jdautilities.oauth2.session.SessionData

class OauthSessionController : SessionController<OauthSession> {
    val sessions = mutableMapOf<String, OauthSession>()

    override fun getSession(identifier: String) = sessions[identifier]

    override fun createSession(data: SessionData): OauthSession {
        val created = OauthSession(data)
        sessions[data.identifier] = created
        return created
    }
}
