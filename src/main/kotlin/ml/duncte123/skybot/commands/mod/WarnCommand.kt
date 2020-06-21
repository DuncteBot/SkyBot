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
import me.duncte123.botcommons.messaging.MessageUtils.sendErrorWithMessage
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.Flag
import ml.duncte123.skybot.objects.guild.WarnAction
import ml.duncte123.skybot.utils.ModerationUtils.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore
import net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class WarnCommand : ModBaseCommand() {

    init {
//        this.shouldLoadMembers = true
//        this.requiresArgs = true
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

    fun execute__(ctx: CommandContext) {
        for (i in 1..260) {
            println(
                "$i: ${getSelectedWarnAction(i, ctx)}"
            )
        }
    }

    override fun execute(ctx: CommandContext) {
        val mentioned = ctx.getMentionedArg(0)

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "I could not find any members with name ${ctx.args[0]}")
            return
        }

        val target = mentioned[0]

        // The bot cannot be warned
        if (target.user == ctx.jda.selfUser) {
            sendErrorWithMessage(ctx.message, "You can not warn me")
            return
        }

        val jdaGuild = ctx.jdaGuild
        val guild = ctx.guild
        val moderator = ctx.member
        val channel = ctx.channel
        val modUser = ctx.author

        // Check if we can interact
        if (!canInteract(moderator, target, "warn", channel)) {
            return
        }

        var reason = ""
        val flags = ctx.getParsedFlags(this)

        if (flags.containsKey("r")) {
            reason = flags["r"]!!.joinToString(" ")
        }

        val dmMessage = """You have been warned by ${modUser.asTag}
            |Reason: ${if (reason.isEmpty()) "No reason given" else "`$reason`"}
        """.trimMargin()

        // add the new warning to the database
        addWarningToDb(ctx.databaseAdapter, modUser, target.user, reason, guild)
        modLog(modUser, target.user, "warned", reason, guild)

        // Yes we can warn bots (cuz why not) but we cannot dm them
        if (!target.user.isBot) {
            target.user.openPrivateChannel()
                .flatMap {  it.sendMessage(dmMessage) }
                .queue(null, ignore(CANNOT_SEND_TO_USER))
        }

        MessageUtils.sendSuccess(ctx.message)


        // Check if the warning count is more than 3
        // and kick the user if this threshold is exceeded
        // TODO: make both the threshold and the action configurable
        val warnCount = getWarningCountForUser(ctx.databaseAdapter, target.user, jdaGuild)
        val action = getSelectedWarnAction(warnCount, ctx)

        if (action != null) {
            invokeAction(warnCount, action, modUser, target, ctx)
        }
    }

    // ideas:
    //     Able to create many actions
    //     Max action of 1 for normal guilds
    //     Max action of 3 for patron guilds
    private fun getSelectedWarnAction(threshold: Int, ctx: CommandContext): WarnAction? {
        return ctx.guild.getSettings()
            .warnActions
            // we reverse the list so we start with the highest one
            // preventing that everything is selected for the lowest number
            .reversed()
            .find { threshold >= it.threshold }
    }

    private fun invokeAction(warnings: Int, action: WarnAction, modUser: User, target: Member, ctx: CommandContext) {
        val guild = ctx.guild

        when (action.type) {
            WarnAction.Type.MUTE -> {}
            WarnAction.Type.TEMP_MUTE -> {}
            WarnAction.Type.KICK -> {
                ctx.jdaGuild.kick(target).reason("Reached $warnings warnings").queue()
                modLog(modUser, target.user, "kicked", "Reached $warnings warnings", guild)
            }
            WarnAction.Type.TEMP_BAN -> {}
            WarnAction.Type.BAN -> {
                ctx.jdaGuild.ban(target, 0).reason("Reached $warnings warnings").queue()
                modLog(modUser, target.user, "banned", "Reached $warnings warnings", guild)
            }
        }
    }

}
