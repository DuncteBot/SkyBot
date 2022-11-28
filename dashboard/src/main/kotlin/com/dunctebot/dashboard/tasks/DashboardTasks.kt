package com.dunctebot.dashboard.tasks

import com.dunctebot.dashboard.controllers.GuildController
import com.dunctebot.dashboard.controllers.api.guildsRequests
import com.dunctebot.dashboard.server
import com.dunctebot.jda.oauth.OauthSessionController
import java.time.OffsetDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DashboardTasks {
    private val threadPool = Executors.newScheduledThreadPool(4) {
        val t = Thread(it, "DashboardTasksThread")
        t.isDaemon = true
        return@newScheduledThreadPool t
    }

    init {
        // start cleaners
        // clean the hashes pool every hour
        threadPool.scheduleAtFixedRate(
            GuildController.guildHashes::cleanUp,
            1,
            1,
            TimeUnit.HOURS
        )
        threadPool.scheduleAtFixedRate(
            GuildController.guildRoleCache::cleanUp,
            1,
            1,
            TimeUnit.HOURS
        )
        // Clean the guilds pool every 30 minutes
        threadPool.scheduleAtFixedRate(
            guildsRequests::cleanUp,
            30,
            30,
            TimeUnit.MINUTES
        )
        // Clean the security keys on a daily basis
        threadPool.scheduleAtFixedRate(
            GuildController.securityKeys::clear,
            1,
            1,
            TimeUnit.DAYS
        )
        // clear the oauth sessions every day
        threadPool.scheduleAtFixedRate({
            val sessionController = server.oAuth2Client.sessionController as OauthSessionController
            val sessions = sessionController.sessions
            val now = OffsetDateTime.now()

            sessions.filterValues { now >= it.expiration }.keys.forEach(sessions::remove)
        }, 1, 1, TimeUnit.DAYS)
    }
}
