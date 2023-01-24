package com.dunctebot.jda.json

import net.dv8tion.jda.api.entities.Guild

class JsonGuild(guild: Guild) {
    val id = guild.id
    val name = guild.name
}
