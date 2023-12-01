package com.dunctebot.dashboard.tasks

import com.dunctebot.dashboard.controllers.GuildController
import com.dunctebot.dashboard.server
import com.dunctebot.jda.oauth.OauthSessionController
import java.time.OffsetDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DashboardTasks {
    private val threadPool = Executors.newScheduledThreadPool(1) {
        val t = Thread(it, "DashboardTasksThread")
        t.isDaemon = true
        return@newScheduledThreadPool t
    }

    init {
        // start cleaners
        // clean the hashes pool every hour
        // TODO: do we need this?
        /*threadPool.scheduleAtFixedRate(
            GuildController.guildHashes::clear,
            1,
            1,
            TimeUnit.HOURS
        )
        threadPool.scheduleAtFixedRate(
            GuildController.guildRoleCache::clear,
            1,
            1,
            TimeUnit.HOURS
        )
        // Clean the guilds pool every 30 minutes
        threadPool.scheduleAtFixedRate(
            guildsRequests::clear,
            30,
            30,
            TimeUnit.MINUTES
        )*/
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
