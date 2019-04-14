/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.core.entities.User

class AvatarCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {
        var user: User? = ctx.author

        if (!ctx.args.isEmpty()) {
            // We're searching for members in the guild to get more accurate results
            val foundMembers = FinderUtil.findMembers(ctx.args[0], ctx.guild)

            user = if (foundMembers.isEmpty()) null else foundMembers[0].user
        }

        if (user == null) {
            sendMsg(ctx, "That user could not be found")

            return
        }

        sendMsg(ctx, "**${user.asTag}'s** avatar:\n${user.effectiveAvatarUrl}?size=2048")
    }

    override fun getName() = "avatar"

    override fun help() = """Shows the avatar for a you or the specified user
        |Usage: `${Settings.PREFIX}$name [@user]`
    """.trimMargin()

}
