/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.ModerationUtils
import net.dv8tion.jda.core.Permission

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class WarnCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (!event.member.hasPermission(Permission.KICK_MEMBERS)) {
            MessageUtils.sendMsg(event, "You don't have permission to run this command")
            MessageUtils.sendError(event.message)
            return
        }

        if (ctx.args.isEmpty() || event.message.mentionedMembers.isEmpty()) {
            MessageUtils.sendMsg(event, "Must mention a member")
            MessageUtils.sendError(event.message)
            return
        }
        val target = event.message.mentionedMembers[0]
        if (target.user == event.jda.selfUser) {
            MessageUtils.sendErrorWithMessage(event.message, "You can not warn me")
            return
        }
        if (!event.member.canInteract(target)) {
            MessageUtils.sendMsg(event, "You can't warn that member because he/she has a higher or equal position than you")
            MessageUtils.sendError(event.message)
            return
        }
        if (ModerationUtils.getWarningCountForUser(ctx.databaseAdapter, target.user, event.guild) >= 3) {
            event.guild.controller.kick(target).reason("Reached 3 warnings").queue()
            ModerationUtils.modLog(event.author, target.user, "kicked", "Reached 3 warnings", ctx.guild)
            return
        }
        var reason = ""
        if (ctx.args.size > 1)
            reason = ctx.argsRaw.replace(target.asMention + " ", "")

        val dmMessage = """You have been warned by ${String.format("%#s", event.author)}
            |Reason: ${if (reason.isEmpty()) "No reason given" else "`$reason`"}
        """.trimMargin()

        ModerationUtils.addWarningToDb(ctx.databaseAdapter, event.author, target.user, reason, event.guild)
        ModerationUtils.modLog(event.author, target.user, "warned", reason, ctx.guild)
        target.user.openPrivateChannel().queue {
            //Ignore the fail consumer, we don't want to have spam in the console
            it.sendMessage(dmMessage).queue(null) { _ -> }
        }
        MessageUtils.sendSuccess(event.message)

    }

    override fun help() = """Warns a member.
        |When a member has 3 warnings he/she will be kicked
        |Usage: `$PREFIX$name <@user> [reason]`
    """.trimMargin()

    override fun getName() = "warn"

}
