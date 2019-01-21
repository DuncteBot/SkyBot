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

package ml.duncte123.skybot.commands.admin

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormat
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.objects.LongPair
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.core.Permission

class VcAutoRoleCommand : ModBaseCommand() {

    init {
        this.category = CommandCategory.ADMINISTRATION
        this.perms = arrayOf(Permission.MANAGE_SERVER)
        this.selfPerms = arrayOf(Permission.MANAGE_SERVER, Permission.MANAGE_ROLES)
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val guild = ctx.guild
        val args = ctx.args
        val vcAutoRoleCache = ctx.variables.vcAutoRoleCache

        if (args.size == 1) {

            if (args[0] != "off") {
                sendMsg(event, "Missing arguments, check `${Settings.PREFIX}help $name`")
                return
            }

            if (!vcAutoRoleCache.containsKey(guild.idLong)) {
                sendMsg(event, "No vc autorole has been set for this server")
                return
            }

            sendMsg(event, "Auto VC Role has been disabled")
            return
        }

        val foundVoiceChannels = FinderUtil.findVoiceChannels(args[0], guild)
        val foundRoles = FinderUtil.findRoles(args[1], guild)

        if (foundVoiceChannels.isEmpty()) {
            sendMsgFormat(
                event,
                "I could not find any voice channels for `%s`%n" +
                    "TIP: If your voice channel name has spaces \"Surround it with quotes\" to give it as one argument",
                args[0]
            )

            return
        }

        if (foundRoles.isEmpty()) {
            sendMsgFormat(
                event,
                "I could not find any role for `%s`%n" +
                    "TIP: If your role name has spaces \"Surround it with quotes\" to give it as one argument",
                args[1]
            )

            return
        }

        val targetChannel = foundVoiceChannels[0]
        val targetRole = foundRoles[0]

        vcAutoRoleCache.put(guild.idLong, LongPair(targetChannel.idLong, targetRole.idLong))

        sendMsgFormat(
            event,
            "Role %s will now be applied to a user when they join <#%s>",
            targetRole, targetChannel.id
        )

    }

    override fun getName() = "vcautorole"

    override fun help() = """Gives a role to a user when they join a specified voice channel
        |Usage: `${Settings.PREFIX}$name <voice channel> <role>` or `${Settings.PREFIX}$name off`
    """.trimMargin()
}
