/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.utils

import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern
import kotlin.streams.toList

@Suppress("HasPlatformType")
object FinderUtils {
    private val logger = LoggerFactory.getLogger(FinderUtils::class.java)

    private val DISCORD_ID = Pattern.compile("\\d{17,20}") // ID
    private val FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})") // $1 -> username, $2 -> discriminator
    private val USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>") // $1 -> ID

    @JvmStatic
    fun searchUsers(input: String, ctx: CommandContext): List<User> {
        var searchId: String? = null
        val userMention = USER_MENTION.matcher(input)

        if (userMention.matches()) {
            searchId = userMention.group(1)

            val mentioned = ctx.message.mentionedUsers.find { it.id == searchId }

            if (mentioned != null) {
                return listOf(mentioned)
            }
        }

        val jda = ctx.shardManager
        val refMatcher = FULL_USER_REF.matcher(input)

        if (refMatcher.matches()) {
            val nameLower = refMatcher.group(1)
            val discrim = refMatcher.group(2)

            val users = jda.userCache.applyStream { s ->
                s.filter {
                    it.name.equals(nameLower, ignoreCase = true) && it.discriminator == discrim
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
                            logger.error("Failed to retrieve member by id", it)
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
            val mentioned = ctx.message.mentionedMembers.find { it.id == searchId }

            if (mentioned != null) {
                return listOf(mentioned)
            }
        }

        val idMatcher = DISCORD_ID.matcher(input)

        if (idMatcher.matches()) {
            searchId = input
        }

        val guild = ctx.jdaGuild

        if (searchId != null) {
            val memberById = guild.getMemberById(searchId)

            return if (memberById != null) {
                listOf(memberById)
            } else {
                val retrieveFuture = CompletableFuture<List<Member>>()

                guild.retrieveMemberById(searchId, false)
                    .queue(
                        { retrieveFuture.complete(listOf(it)) },
                        {
                            logger.error("Failed to retrieve member by id", it)
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

        return future.get()
    }
}
