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

import gnu.trove.list.TLongList
import gnu.trove.list.array.TLongArrayList
import gnu.trove.map.hash.TLongObjectHashMap
import me.duncte123.botcommons.text.TextColor
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.entities.jda.DunctebotGuild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import org.slf4j.LoggerFactory
import java.util.stream.Collectors

class SpamFilter(private val variables: Variables) : TLongObjectHashMap<SpamCache>() {
    private lateinit var rates: LongArray
    private val database = variables.database
    private val logger = LoggerFactory.getLogger(SpamFilter::class.java)

    @Throws(IllegalArgumentException::class)
    fun update(longs: LongArray, updateMode: Int = 0) {
        if (this.containsKey(longs[0])) {
            this[longs[0]]!!.update(longs.copyOfRange(1, 3), updateMode)
        } else {
            this.put(longs[0], (SpamCache().update(longs.copyOfRange(1, 3), updateMode)))
        }
    }

    fun clearMessages() {
        for (guildsSpamCache in this.valueCollection()) {
            for (memberId in guildsSpamCache.keys()) {
                guildsSpamCache.put(memberId, TLongArrayList())
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun plus(any: Any?): Any {
        return when (any) {
            null -> {
                this
            }
            is Triple<*, *, *> -> {
                if (any.first is Member && any.second is Message && any.third is Boolean) {
                    return check(any as Triple<Member, Message, Boolean>)
                }
                this
            }
            is Pair<*, *> -> {
                if (any.first is Member && any.second is Message) {
                    val member = any.first as Member
                    return check(Triple(member, any.second as Message, false))
                }
                this
            }
            is LongArray -> {
                applyRates(any)
            }
            is TLongList -> {
                applyRates(any)
            }
            else -> {
                this
            }
        }
    }

    /**
     * @return {@code true} when the message is spam.
     */
    infix fun check(data: Triple<Member, Message, Boolean>): Boolean {
        val author = data.first
        val guild = DunctebotGuild(author.guild, variables)
        val user = author.user
        val msg = data.second
        val jda = msg.jda
        val displayContent = msg.contentDisplay

        val returnValue: Boolean = when {
            displayContent.isBlank() -> {
                if (msg.embeds.isEmpty()) {
                    true
                } else {
                    return msg.embeds.map {
                        it.description.isNullOrBlank() &&
                            it.footer?.text.isNullOrBlank() &&
                            it.title.isNullOrBlank() &&
                            it.thumbnail?.url.isNullOrBlank() &&
                            it.image?.url.isNullOrBlank()
                    }.count { it } < 1
                }
            }
            displayContent.matches("^.(?<![?!.])(?![wola])(?!(\\d|x|D|k|h|\\.{1,2}))".toRegex()) -> {
                true
            }
            else -> {
                logger.debug("${TextColor.CYAN_BACKGROUND}Message with Activity!!${TextColor.RESET}")
                false
            }
        }

        if (returnValue) {
            this.update(longArrayOf(guild.idLong, user.idLong, msg.idLong), 0)
            val cache = this[guild.idLong]
            var shouldModerate = false

            if (cache != null) {
                val msgs = cache[user.idLong]

                if (msgs != null) {
                    if (msgs.size() > guild.settings.spamThreshold) {
                        shouldModerate = true
                    }
                }
            }

            if (shouldModerate) {
                val warnings = database.getWarningCountForUser(user.idLong, guild.idLong).get() + 1

                if (rates.size < 6) {
                    logger.error("Found invalid spam rate settings for " + author.guild)

                    return false
                }

                val ratelimit = rates[warnings.coerceIn(0, 5)]

                database.createWarning(
                    jda.selfUser.idLong,
                    user.idLong,
                    guild.idLong,
                    "Spam"
                )

                val textChannel = msg.channel.asGuildMessageChannel()

                if (data.third) {
                    ModerationUtils.kickUser(guild, author, textChannel, "Spam", false)
                } else {
                    ModerationUtils.muteUser(guild, author, textChannel, "Spam", ratelimit, false)
                }

                val clearable = textChannel.iterableHistory.stream()
                    .filter { it.author == author.user }
                    .limit(9)
                    .collect(Collectors.toList())

                textChannel.deleteMessages(clearable).queue {
                    this[guild.idLong]?.get(author.user.idLong)?.grep { value -> !clearable.map { l -> l.idLong }.contains(value) }
                }

                return true
            }
        }

        return false
    }

    fun applyRates(newRates: LongArray): SpamFilter {
        rates = newRates
        return this
    }

    fun applyRates(newRates: TLongList): SpamFilter {
        rates = newRates.toArray()
        return this
    }
}
