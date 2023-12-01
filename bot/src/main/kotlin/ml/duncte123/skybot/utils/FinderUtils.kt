/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.utils

import ml.duncte123.skybot.extensions.sync
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

object FinderUtils {
    private val logger = LoggerFactory.getLogger(FinderUtils::class.java)

    private val DISCORD_ID = Pattern.compile("\\d{17,20}") // ID
    private val FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})") // $1 -> username, $2 -> discriminator
    private val USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>") // $1 -> ID

    @JvmStatic
    fun searchAudioChannels(input: String, ctx: CommandContext): List<AudioChannel> {
        if (DISCORD_ID.matcher(input).matches()) {
            val channel: AudioChannel? = ctx.guild.getChannelById(AudioChannel::class.java, input)

            if (channel != null) {
                return listOf(channel)
            }
        }

        val vcs = internalAudioChannelSearch(input, ctx.guild.voiceChannelCache)

        if (vcs.isNotEmpty()) {
            return vcs
        }

        return internalAudioChannelSearch(input, ctx.guild.stageChannelCache)
    }

    @JvmStatic
    private fun internalAudioChannelSearch(input: String, cache: SortedSnowflakeCacheView<out AudioChannel>): List<AudioChannel> {
        val exact = mutableListOf<AudioChannel>()
        val wrongCase = mutableListOf<AudioChannel>()
        val startsWith = mutableListOf<AudioChannel>()
        val contains = mutableListOf<AudioChannel>()

        val inputLower = input.lowercase()

        cache.forEach {
            val name = it.name

            if (name == input) {
                exact.add(it)
            } else if (exact.isEmpty() && name.equals(input, true)) {
                wrongCase.add(it)
            } else if (wrongCase.isEmpty() && name.startsWith(inputLower, ignoreCase = true)) {
                startsWith.add(it)
            } else if (startsWith.isEmpty() && name.contains(inputLower, ignoreCase = true)) {
                contains.add(it)
            }
        }

        if (exact.isNotEmpty()) {
            return exact
        }

        if (wrongCase.isNotEmpty()) {
            return wrongCase
        }

        if (startsWith.isNotEmpty()) {
            return startsWith
        }

        return contains
    }

    @JvmStatic
    fun searchUsers(input: String, ctx: CommandContext): List<User> {
        var searchId: String? = null
        val userMention = USER_MENTION.matcher(input)

        if (userMention.matches()) {
            searchId = userMention.group(1)

            val mentioned = ctx.message.mentions.users.find { it.id == searchId }

            if (mentioned != null) {
                return listOf(mentioned)
            }
        }

        val jda = ctx.shardManager
        val refMatcher = FULL_USER_REF.matcher(input)

        if (refMatcher.matches()) {
            val name = refMatcher.group(1)
            val discrim = refMatcher.group(2)

            val users = jda.userCache.applyStream { s ->
                s.filter {
                    it.name.equals(name, ignoreCase = true) && it.discriminator == discrim
                }.toList()
            }!!

            if (users.isNotEmpty()) {
                return users
            }
        }

        val idMatcher = DISCORD_ID.matcher(input)

        if (idMatcher.matches()) {
            searchId = input
        }

        if (searchId != null) {
            val userById = jda.getUserById(searchId)

            return if (userById == null) {
                val retrieveFuture = CompletableFuture<List<User>>()

                jda.retrieveUserById(searchId)
                    .queue(
                        { retrieveFuture.complete(listOf(it)) },
                        {
                            if (it is ErrorResponseException && it.errorResponse != ErrorResponse.UNKNOWN_USER) {
                                // only log if it's not unknown user
                                logger.error("Failed to retrieve user by id", it)
                            }

                            retrieveFuture.complete(listOf())
                        }
                    )

                retrieveFuture.get()
            } else {
                listOf(userById)
            }
        }

        return listOf()
    }

    @JvmStatic
    fun searchMembers(input: String, ctx: CommandContext): List<Member> {
        var searchId: String? = null
        val mentionMatcher = USER_MENTION.matcher(input)

        if (mentionMatcher.matches()) {
            searchId = mentionMatcher.group(1)

            // Don't use ctx.mentionedMembers as it calls this function
            val mentioned = ctx.message.mentions.members.find { it.id == searchId }

            if (mentioned != null) {
                return listOf(mentioned)
            }
        }

        val idMatcher = DISCORD_ID.matcher(input)

        if (idMatcher.matches()) {
            searchId = input
        }

        val guild = ctx.jdaGuild
        val refMatcher = FULL_USER_REF.matcher(input)

        if (refMatcher.matches()) {
            val name = refMatcher.group(1)
            val discrim = refMatcher.group(2)

            val membersByTag = guild.findMembers {
                it.user.name.equals(name, ignoreCase = true) && it.user.discriminator == discrim
            }.sync()

            if (membersByTag.isNotEmpty()) {
                return membersByTag
            }
        }

        if (searchId != null) {
            val memberById = guild.getMemberById(searchId)

            return if (memberById != null) {
                listOf(memberById)
            } else {
                val retrieveFuture = CompletableFuture<List<Member>>()

                guild.retrieveMemberById(searchId)
                    .queue(
                        { retrieveFuture.complete(listOf(it)) },
                        {
                            if (
                                it is ErrorResponseException &&
                                it.errorResponse != ErrorResponse.UNKNOWN_USER &&
                                it.errorResponse != ErrorResponse.UNKNOWN_MEMBER
                            ) {
                                // only log if it's not unknown user/member
                                logger.error("Failed to retrieve member by id", it)
                            }

                            retrieveFuture.complete(listOf())
                        }
                    )

                retrieveFuture.get()
            }
        }

        val future = CompletableFuture<List<Member>>()

        guild.retrieveMembersByPrefix(input, 10)
            .onSuccess { future.complete(it) }
            // Empty list on error
            .onError {
                logger.error("Failed to retrieve member by prefix", it)
                future.complete(listOf())
            }

        // Lol this actually breaks behavior
//        return guild.retrieveMembersByPrefix(input, 10).sync()
        return future.get()
    }
}
