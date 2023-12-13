package com.dunctebot.dashboard.tasks

import com.dunctebot.dashboard.controllers.GuildController
import com.dunctebot.dashboard.server
import com.dunctebot.dashboard.systemPool
import com.dunctebot.jda.oauth.OauthSessionController
import ml.duncte123.skybot.utils.ThreadUtils.runOnVirtual
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class DashboardTasks {
    init {
        // start cleaners
        // clean the hashes pool every hour
        // TODO: do we need this?
        /*systemPool.scheduleAtFixedRate(
            { runOnVirtual(GuildController.guildHashes::clear) },
            1,
            1,
            TimeUnit.HOURS
        )
        systemPool.scheduleAtFixedRate(
            { runOnVirtual(GuildController.guildRoleCache::clear) },
            1,
            1,
            TimeUnit.HOURS
        )
        // Clean the guilds pool every 30 minutes
        systemPool.scheduleAtFixedRate(
            { runOnVirtual(guildsRequests::clear) },
            30,
            30,
            TimeUnit.MINUTES
        )*/
        // Clean the security keys on a daily basis
        systemPool.scheduleAtFixedRate(
            { runOnVirtual(GuildController.securityKeys::clear) },
            1,
            1,
            TimeUnit.DAYS
        )
        // clear the oauth sessions every day
        systemPool.scheduleAtFixedRate({ runOnVirtual {
            val sessionController = server.oAuth2Client.sessionController as OauthSessionController
            val sessions = sessionController.sessions
            val now = OffsetDateTime.now()

            sessions.filterValues { now >= it.expiration }.keys.forEach(sessions::remove)
        } }, 1, 1, TimeUnit.DAYS)
    }
}
