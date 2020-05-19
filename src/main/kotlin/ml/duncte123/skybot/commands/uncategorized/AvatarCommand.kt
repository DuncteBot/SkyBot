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

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import java.util.regex.Pattern

class AvatarCommand : Command() {
    val DISCORD_ID = Pattern.compile("\\d{17,20}") // ID
    val FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})") // $1 -> username, $2 -> discriminator
    val USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>") // $1 -> ID


    init {
        this.name = "avatar"
        this.help = "Shows your avatar or the one for the specified user"
        this.usage = "[@user]"
    }

    override fun execute(ctx: CommandContext) {
        var user: User? = ctx.author

        if (ctx.args.isNotEmpty()) {
            val fromFinderUtil = FinderUtil.findMembers(ctx.argsRaw, ctx.jdaGuild)
            // We're searching for members in the guild to get more accurate results
            val foundMembers = this.searchMembers(ctx.argsRaw, ctx.jdaGuild)

            println(foundMembers)
            println(fromFinderUtil)

            user = if (foundMembers.isEmpty()) {
                val foundUsers = FinderUtil.findUsers(ctx.argsRaw, ctx.jda)

                if (foundUsers.isNotEmpty()) foundUsers[0] else null
            } else foundMembers[0].user
        }

        if (user == null) {
            sendMsg(ctx, "That user could not be found")

            return
        }

        sendMsg(ctx, "**${user.asTag}'s** avatar:\n${user.effectiveAvatarUrl}?size=2048")
    }

    private fun searchMembers(input: String, guild: Guild): List<Member> {
        val mentionMatcher = USER_MENTION.matcher(input)

        if (mentionMatcher.matches()) {
            return guild.retrieveMembersByIds(false, mentionMatcher.group(1)).get()
        }

        return guild.retrieveMembersByPrefix(input, 10).get()
    }

    private fun searchMembersAsync(input: String, guild: Guild, callback: (List<Member>) -> Unit) {
        var searchId: String? = null
        val mentionMatcher = USER_MENTION.matcher(input)
        val idMatcher = DISCORD_ID.matcher(input)

        if (mentionMatcher.matches()) {
            searchId = mentionMatcher.group(1)
        } else if (idMatcher.matches()) {
            searchId = input
        }

        if (searchId != null) {
            guild.retrieveMembersByIds(false, searchId).onSuccess(callback)

            return
        }

        guild.retrieveMembersByPrefix(input, 10).onSuccess(callback)
    }
}
