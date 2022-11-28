package com.dunctebot.jda.json

import net.dv8tion.jda.api.entities.TextChannel

class JsonChannel(channel: TextChannel) {
    val id = channel.id
    val name = channel.name
}
