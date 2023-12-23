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

package me.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.skybot.objects.command.Command
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.utils.FinderUtils
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

class AvatarCommand : Command() {
    init {
        this.name = "avatar"
        this.aliases = arrayOf("av", "pfp", "picture", "profilepicture")
        this.help = "Shows your avatar or the one for the specified user"
        this.usage = "[@user]"
    }

    override fun execute(ctx: CommandContext) {
        var user: User? = ctx.author
        var member: Member? = null

        if (ctx.args.isNotEmpty()) {
            // We're searching for members in the guild to get more accurate results
            val foundMembers = FinderUtils.searchMembers(ctx.argsRaw, ctx)

            user = if (foundMembers.isEmpty()) {
                val foundUsers = FinderUtils.searchUsers(ctx.argsRaw, ctx)

                if (foundUsers.isNotEmpty()) foundUsers[0] else null
            } else {
                member = foundMembers[0]
                member.user
            }
        }

        if (user == null) {
            sendMsg(ctx, "That user could not be found")

            return
        }

        val avUrl = member?.effectiveAvatarUrl ?: user.effectiveAvatarUrl

        sendMsg(ctx, "**${user.asTag}'s** avatar:\n$avUrl?size=4096")
    }
}
