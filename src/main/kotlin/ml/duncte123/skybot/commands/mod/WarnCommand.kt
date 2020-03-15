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

package ml.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.Flag
import ml.duncte123.skybot.utils.ModerationUtils.*
import net.dv8tion.jda.api.Permission

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class WarnCommand : ModBaseCommand() {

    init {
        this.requiresArgs = true
        this.name = "warn"
        this.help = "Warns a user\nWhen a user has reached 3 warnings they will be kicked from the server"
        this.usage = "<@user> [-r reason]"
        this.userPermissions = arrayOf(Permission.KICK_MEMBERS)
        this.flags = arrayOf(
            Flag(
                'r',
                "reason",
                "Sets the reason for this warning"
            )
        )
    }

    override fun execute(ctx: CommandContext) {
        val event = ctx.event
        val mentioned = ctx.getMentionedArg(0)

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "I could not find any members with name ${ctx.args[0]}")
            return
        }

        val target = mentioned[0]

        if (target.user == event.jda.selfUser) {
            MessageUtils.sendErrorWithMessage(event.message, "You can not warn me")
            return
        }

        if (!canInteract(ctx.member, target, "warn", ctx.channel)) {
            return
        }

        if (getWarningCountForUser(ctx.databaseAdapter, target.user, event.guild) >= 3) {
            event.guild.kick(target).reason("Reached 3 warnings").queue()
            modLog(event.author, target.user, "kicked", "Reached 3 warnings", ctx.guild)
            return
        }

        var reason = ""
        val flags = ctx.getParsedFlags(this)

        if (flags.containsKey("r")) {
            reason = flags["r"]!!.joinToString(" ")
        }

        val dmMessage = """You have been warned by ${event.author.asTag}
            |Reason: ${if (reason.isEmpty()) "No reason given" else "`$reason`"}
        """.trimMargin()

        addWarningToDb(ctx.databaseAdapter, event.author, target.user, reason, event.guild)
        modLog(event.author, target.user, "warned", reason, ctx.guild)

        if (!target.user.isBot) {
            target.user.openPrivateChannel().queue {
                //Ignore the fail consumer, we don't want to have spam in the console
                it.sendMessage(dmMessage).queue(null) { }
            }
        }

        MessageUtils.sendSuccess(event.message)

    }

}
