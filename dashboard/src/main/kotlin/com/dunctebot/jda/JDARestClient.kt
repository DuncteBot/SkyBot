package com.dunctebot.jda

import com.dunctebot.jda.impl.MemberPaginationActionImpl
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.requests.CompletedRestAction
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.requests.Route
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig
import net.dv8tion.jda.internal.utils.config.MetaConfig
import net.dv8tion.jda.internal.utils.config.SessionConfig
import net.dv8tion.jda.internal.utils.config.ThreadingConfig
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * Custom jda rest client that allows for rest-only usage of JDA
 *
 * This class has been inspired by GivawayBot and all credit goes to them https://github.com/jagrosh/GiveawayBot
 */
class JDARestClient(token: String) {
    // create a guild cache that keeps the guilds in cache for 30 minutes
    // When we stop accessing the guild it will be removed from the cache
    private val guildCache = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expiration(30, TimeUnit.MINUTES)
        .build<String, Guild>()

    val jda: JDAImpl

    init {
        val authConfig = AuthorizationConfig(token)
        val sessionConfig = SessionConfig.getDefault()
        val threadConfig = ThreadingConfig.getDefault()
        val metaConfig = MetaConfig.getDefault()

        threadConfig.setRateLimitPool(Executors.newScheduledThreadPool(5) {
            val t = Thread(it, "dunctebot-rest-thread")
            t.isDaemon = true
            return@newScheduledThreadPool t
        }, true)

        jda = JDAImpl(authConfig, sessionConfig, threadConfig, metaConfig)

        retrieveSelfUser().queue(jda::setSelfUser)
    }

    fun invalidateGuild(guildId: Long) {
        jda.guildsView.remove(guildId)
        guildCache.remove(guildId.toString())
    }

    fun retrieveUserById(id: String): RestAction<User> {
        val route = Route.Users.GET_USER.compile(id)

        return RestActionImpl(jda, route) {
            response, _ -> jda.entityBuilder.createUser(response.getObject())
        }
    }

    private fun retrieveSelfUser(): RestAction<SelfUser> {
        val route = Route.Self.GET_SELF.compile()

        return RestActionImpl(jda, route) {
            response, _ -> jda.entityBuilder.createSelfUser(response.getObject())
        }
    }

    fun retrieveAllMembers(guild: Guild): MemberPaginationAction = MemberPaginationActionImpl(guild)

    private fun retrieveGuildChannelsArray(guildId: String): RestAction<DataArray> {
        val route = Route.Guilds.GET_CHANNELS.compile(guildId)

        return RestActionImpl(jda, route) { response, _ -> response.array }
    }

    fun retrieveMemberById(guild: Guild, memberId: String): RestAction<Member> {
        val route = Route.Guilds.GET_MEMBER.compile(guild.id, memberId)

        return RestActionImpl(jda, route) {
            response, _ -> jda.entityBuilder.createMember(guild as GuildImpl, response.getObject())
        }
    }

    fun retrieveGuildById(id: String): RestAction<Guild> {
        // We're caching two events here, is that worth it?
        /*// Lookup the guild from the cache
        val guildById = jda.getGuildById(id)

        // If we already have it we will return the cached guild
        // TODO: invalidation of caches
        if (guildById != null) {
            return CompletedRestAction(jda, guildById)
        }*/

        // Temp cache
        val cachedGuild = guildCache[id]

        if (cachedGuild != null && !cachedGuild.textChannelCache.isEmpty) {
            return CompletedRestAction(jda, cachedGuild)
        }

        val route = Route.Guilds.GET_GUILD.compile(id).withQueryParams("with_counts", "true")

        // if the first rest action fails the second one will never be called
        return retrieveGuildChannelsArray(id).flatMap { channels ->
            RestActionImpl(jda, route) { response, _ ->
                val data = response.getObject()

                // fake a bit of data
                data.put("channels", channels)
                data.put("voice_states", DataArray.empty())

                val guild = jda.entityBuilder
                    .createGuild(id.toLong(), data, MiscUtil.newLongMap(), data.getInt("approximate_member_count"))

                val selfMember = retrieveMemberById(guild, jda.selfUser.id).complete()
                guild.membersView.writeLock().use {
                    guild.membersView.map.put(jda.selfUser.idLong, selfMember)
                }

                guildCache[id] = guild

                guild
            }
        }
    }
}
