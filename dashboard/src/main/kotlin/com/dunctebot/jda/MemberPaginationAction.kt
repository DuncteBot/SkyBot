package com.dunctebot.jda

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction

interface MemberPaginationAction : PaginationAction<Member, MemberPaginationAction> {
    val guild: Guild
}
