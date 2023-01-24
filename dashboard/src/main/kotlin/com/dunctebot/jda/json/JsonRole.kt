package com.dunctebot.jda.json

import net.dv8tion.jda.api.entities.Role

open class JsonRole(role: Role) {
    val id = role.id
    val name = role.name
}
