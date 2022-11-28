package com.dunctebot.jda.oauth

import com.jagrosh.jdautilities.oauth2.Scope
import com.jagrosh.jdautilities.oauth2.session.Session
import com.jagrosh.jdautilities.oauth2.session.SessionData
import java.time.OffsetDateTime

class OauthSession constructor(
    private val accessToken: String,
    private val refreshToken: String,
    private val tokenType: String,
    private val expiration: OffsetDateTime,
    private val scopes: Array<Scope>
) : Session {
    constructor(data: SessionData) : this(
        data.accessToken,
        data.refreshToken,
        data.tokenType,
        data.expiration,
        data.scopes
    )

    override fun getAccessToken() = accessToken
    override fun getRefreshToken() = refreshToken
    override fun getScopes() = scopes
    override fun getTokenType() = tokenType
    override fun getExpiration() = expiration
}
