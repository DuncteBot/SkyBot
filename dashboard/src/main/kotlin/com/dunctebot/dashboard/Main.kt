package com.dunctebot.dashboard

import com.dunctebot.dashboard.tasks.DashboardTasks
import org.slf4j.LoggerFactory

fun main() {
    Runtime.getRuntime().addShutdownHook(Thread {
        server.shutdown()
    })

    val logger = LoggerFactory.getLogger("Main")

    // start tasks
    DashboardTasks()

    server.start()

    logger.info("Dashboard ready")
}
