/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import me.duncte123.botCommons.text.TextColor
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import org.slf4j.LoggerFactory
import java.util.stream.Collectors

class SpamFilter : HashMap<Long, SpamCache>() {

    private lateinit var rates: LongArray

    @Throws(IllegalArgumentException::class)
    public fun update(longs: LongArray, updateMode: Int = 0) {
        if (this.containsKey(longs[0]))
            this[longs[0]]!!.update(longs.copyOfRange(1, 3), updateMode)
        else {
            this[longs[0]] = (SpamCache().update(longs.copyOfRange(1, 3), updateMode))
        }
    }

    public fun clearMessages() {
        for (guildsSpamCache in this.values) {
            for (memberId in guildsSpamCache.keys) {
                guildsSpamCache[memberId] = ArrayList()
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
                    return check(Triple(any.first as Member, any.second as Message, false))
                }
                this
            }
            is LongArray -> {
                applyRates(any)
            }
            is List<*> -> {
                applyRates(any.filter { it is Long }.map { it as Long })
            }
            else -> {
                this
            }
        }
    }

    /**
     * @return {@code true} when the message is spam.
     */
    public infix fun check(data: Triple<Member, Message, Boolean>): Boolean {
        val author = data.first
        val guild = author.guild
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
                        it.description.isBlank()
                                && it.footer.text.isBlank()
                                && it.title.isBlank()
                                && it.thumbnail.url.isBlank()
                                && it.image.url.isBlank()
                    }.count { it } < 1
                }
            }
            displayContent.matches("^.(?<![?!.])(?![wola])(?!(\\d|x|D|k|h|\\.{1,2}))".toRegex()) -> {
                true
            }
            else -> {
                LoggerFactory.getLogger(SpamFilter::class.java).debug("${TextColor.CYAN_BACKGROUND}Message with Activity!!${TextColor.RESET}")
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
                    if (msgs.size > 7)
                        shouldModerate = true
                }
            }

            if (shouldModerate) {
                val warnings = ModerationUtils.getWarningCountForUser(user, author.guild) + 1
                val ratelimit = rates[warnings.coerceIn(0, 5)]
                ModerationUtils.addWarningToDb(jda.selfUser, user, "Spam", guild, jda)
                if (data.third) {
                    ModerationUtils.kickUser(guild, author, msg.textChannel, "Spam")
                } else {
                    ModerationUtils.muteUser(guild, author, msg.textChannel, "Spam", ratelimit)
                }
                val clearable = msg.textChannel.iterableHistory.stream().filter { it.author == author.user }.limit(9).collect(Collectors.toList())
                msg.textChannel.deleteMessages(clearable).queue {
                    this[guild.idLong]?.get(author.user.idLong)?.filter { !clearable.map { it.idLong }.contains(it) }
                }

                return true
            }
        }

        return false
    }

    public fun applyRates(newRates: LongArray): SpamFilter {
        rates = newRates
        return this
    }

    public fun applyRates(newRates: List<Long>): SpamFilter {
        rates = newRates.toLongArray()
        return this
    }

}
