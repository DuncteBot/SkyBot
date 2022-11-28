package com.dunctebot.dashboard.bodies

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PatronBody(val userId: String?, val guildId: String?, val token: String?)
