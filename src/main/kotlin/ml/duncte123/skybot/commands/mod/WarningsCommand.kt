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

package ml.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.Permission

class WarningsCommand : ModBaseCommand() {

    init {
        this.requiresArgs = true
        this.name = "warnings"
        this.helpFunction = { _, _ -> "Shows the active warnings that a member has" }
        this.usageInstructions = { prefix, invoke -> "`$prefix$invoke <@user>`" }
        this.userPermissions = arrayOf(Permission.KICK_MEMBERS)
    }

    override fun execute(ctx: CommandContext) {
        val mentioned = ctx.getMentionedArg(0)
        val db = ctx.databaseAdapter

        if (mentioned.isEmpty()) {
            this.sendUsageInstructions(ctx)

            return
        }

        val member = mentioned[0]

        db.getWarningsForUser(member.idLong, ctx.guild.idLong) { warnings ->
            if (warnings.isEmpty()) {
                sendMsg(ctx, "This member has no active warnings")

                return@getWarningsForUser
            }

            val out = buildString {
                warnings.forEach {
                    val mod = ctx.jda.getUserById(it.modId)
                    val modName = mod?.asTag ?: "Unknown#0000"
                    val reason = if (it.reason.isNotBlank()) it.reason else "None"

                    appendln("`[${it.rawDate}]` Reason: _${reason}_ by $modName")
                }
            }

            sendEmbed(ctx, EmbedUtils.embedMessage(out))
        }
    }
}
