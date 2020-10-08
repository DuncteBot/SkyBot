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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.CommandUtils
import ml.duncte123.skybot.utils.CommandUtils.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

class PatreonCheckCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
        this.name = "patroncheck"
    }

    override fun execute(ctx: CommandContext) {
        val user = ctx.author
        val guild = ctx.jdaGuild

        val message = """Patreon access checks:```diff
            |${true.a("default")}
            |${getIsPatronStatus(user).a("patron")}
            |${getIsFullPatronStatus(user).a("patron_full")}
            |${isUserTagPatron(user).a("tag_patron")}
            |${getIsGuildPatronStatus(user, guild).a("guild_patron")}
            |${getIsGuildPremium(guild).a("guild_premium")}
            |${ONEGUILD_PATRONS.containsValue(guild.idLong).a("guild_one_guild")}
            |${isUserOrGuildPatron(ctx.event, false).a("isUserOrGuildPatron (should be true if any is true)")}
            |```
        """.trimMargin()

        sendMsg(ctx, message)
    }

    private fun Boolean.a(b: String) = "${if (this) "+" else "-"} $b: ${if (this) "yes" else "no"}"

    private fun getIsPatronStatus(user: User): Boolean {
        val klass = CommandUtils::class.java
        val method = klass.getDeclaredMethod("isPatron", User::class.java, TextChannel::class.java)

        method.isAccessible = true

        return method.invoke(null, user, null) as Boolean
    }

    private fun getIsGuildPatronStatus(user: User, guild: Guild): Boolean {
        val klass = CommandUtils::class.java
        val method = klass.getDeclaredMethod("isGuildPatron", User::class.java, Guild::class.java)

        method.isAccessible = true

        return method.invoke(null, user, guild) as Boolean
    }

    private fun getIsFullPatronStatus(user: User): Boolean {
        val klass = CommandUtils::class.java
        val method = klass.getDeclaredMethod("isPatron", User::class.java, TextChannel::class.java, Boolean::class.java)

        method.isAccessible = true

        return method.invoke(null, user, null, false) as Boolean
    }

    private fun getIsGuildPremium(guild: Guild): Boolean {
        val klass = CommandUtils::class.java
        val method = klass.getDeclaredMethod("shouldGuildBeConsideredPremium", Guild::class.java)

        method.isAccessible = true

        return method.invoke(null, guild) as Boolean
    }
}
