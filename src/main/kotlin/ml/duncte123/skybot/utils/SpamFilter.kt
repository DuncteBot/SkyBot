package ml.duncte123.skybot.utils

import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import org.slf4j.LoggerFactory

class SpamFilter : HashMap<Long, SpamCache>() {

    private lateinit var rates: LongArray

    @Throws(IllegalArgumentException::class)
    public fun update(longs: LongArray, updateMode: Int = 0) {
        if (this.containsKey(longs[0]))
            this[longs[0]]!!.update(longs.copyOfRange(1, 2), updateMode)
        else {
            this + (longs[0] to (SpamCache().update(longs.copyOfRange(1, 2), updateMode)))
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun plus(any: Any?): Any {
        return when (any) {
            null -> {
                this
            }
            is Pair<*, *> -> {
                if (any.first is Member && any.second is Message) {
                    return check(any as Pair<Member, Message>)
                }
                this
            }
            is LongArray -> {
                rates = any
                this
            }
            is List<*> -> {
                rates = any.filter { it is Long }.map { it as Long }.toLongArray()
                this
            }
            else -> {
                this
            }
        }
    }

    /**
     * @return {@code true} when the message is spam.
     */
    public infix fun check(data: Pair<Member, Message>): Boolean {
        val author = data.first
        val guild = author.guild
        val user = author.user
        val msg = data.second
        val jda = msg.jda
        val rawContent = msg.contentRaw

        val returnValue: Boolean = when {
            rawContent.isBlank() -> {
                if (msg.embeds.isEmpty()) {
                    true
                }
                else {
                    return msg.embeds.map {
                        it.description.isBlank()
                        && it.footer.text.isBlank()
                        && it.title.isBlank()
                        && it.thumbnail.url.isBlank()
                        && it.image.url.isBlank()
                    }.count {it} < 1
                }
            }
            else -> {
                LoggerFactory.getLogger(SpamFilter::class.java).debug("${TextColor.CYAN_BACKGROUND}Message with Activity!!${TextColor.RESET}")
                false
            }
        }

        if (returnValue) {
            this.update(longArrayOf(guild.idLong, user.idLong, msg.idLong), 0)
            val warnings = ModerationUtils.getWarningCountForUser(user, author.guild) + 1
            val ratelimit = rates[warnings.coerceIn(0, 5)]
            ModerationUtils.addWarningToDb(jda.selfUser, user, "Spam", guild, jda)
            ModerationUtils.muteUser(jda, guild, author, msg.textChannel, "Spam", ratelimit)
        }

        return returnValue
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