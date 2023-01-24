package com.dunctebot.jda.json

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

class JsonChannel(channel: TextChannel) {
    val id = channel.id
    val name = channel.name
}
