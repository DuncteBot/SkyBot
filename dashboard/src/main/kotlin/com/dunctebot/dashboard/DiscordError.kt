package com.dunctebot.dashboard

enum class DiscordError(val title: String, val component: String) {
    NO_GUILD("Server not found", "error-discord-no-guild"),
    NO_PERMS("You are missing some permissions!", "error-discord-no-perms"),
    WAT("WAT", "error-discord-wat"),
    ;
}
