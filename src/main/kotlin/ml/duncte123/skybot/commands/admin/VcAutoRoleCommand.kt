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
import gnu.trove.map.hash.TLongLongHashMap
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
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

            if (!vcAutoRoleCache.containsKey(guild.idLong)) {
                sendMsg(event, "No vc autorole has been set for this server")
                return
            }

            if (args[0] == "list") {
                listAutoVcRoles(ctx)
                return
            }

            if (args[0] != "off") {
                sendMsg(event, "Missing arguments, check `${Settings.PREFIX}help $name`")
                return
            }

            vcAutoRoleCache.remove(guild.idLong)
            ctx.databaseAdapter.removeVcAutoRoleForGuild(guild.idLong)

            sendMsg(event, "All Auto VC Roles has been disabled")
            return
        }

        if (args.size > 1) {

            if (args[0] == "add") {
                addVcAutoRole(ctx)
                return
            }

            if (args[0] == "remove") {
                removeVcAutoRole(ctx)
                return
            }

        }

        sendMsg(event, "Unknown operation, check `${Settings.PREFIX}$name`")

    }

    override fun getName() = "vcautorole"

    override fun help() = """Gives a role to a user when they join a specified voice channel
        |Usage: `${Settings.PREFIX}$name add <voice channel> <role>`
        |`${Settings.PREFIX}$name remove <voice channel>`
        |`${Settings.PREFIX}$name off`
        |`${Settings.PREFIX}$name list`
    """.trimMargin()

    private fun removeVcAutoRole(ctx: CommandContext) {
        val event = ctx.event
        val guild = ctx.guild
        val args = ctx.args
        val vcAutoRoleCache = ctx.variables.vcAutoRoleCache
        val cache = vcAutoRoleCache.get(guild.idLong) ?: vcAutoRoleCache.put(guild.idLong, TLongLongHashMap())

        val foundVoiceChannels = FinderUtil.findVoiceChannels(args[1], guild)

        if (foundVoiceChannels.isEmpty()) {
            sendMsgFormat(
                event,
                "I could not find any voice channels for `%s`%n" +
                    "TIP: If your voice channel name has spaces \"Surround it with quotes\" to give it as one argument",
                args[1]
            )

            return
        }

        val targetChannel = foundVoiceChannels[0].idLong

        if (!cache.containsKey(targetChannel)) {
            sendMsg(event, "This voice channel does not have an autorole set")
            return
        }

        cache.remove(targetChannel)
        ctx.databaseAdapter.removeVcAutoRole(targetChannel)
        sendMsg(event, "Autorole removed for <#$targetChannel>")
    }

    private fun addVcAutoRole(ctx: CommandContext) {
        val event = ctx.event
        val guild = ctx.guild
        val args = ctx.args
        val vcAutoRoleCache = ctx.variables.vcAutoRoleCache

        val foundVoiceChannels = FinderUtil.findVoiceChannels(args[1], guild)
        val foundRoles = FinderUtil.findRoles(args[2], guild)

        if (foundVoiceChannels.isEmpty()) {
            sendMsgFormat(
                event,
                "I could not find any voice channels for `%s`%n" +
                    "TIP: If your voice channel name has spaces \"Surround it with quotes\" to give it as one argument",
                args[1]
            )

            return
        }

        if (foundRoles.isEmpty()) {
            sendMsgFormat(
                event,
                "I could not find any role for `%s`%n" +
                    "TIP: If your role name has spaces \"Surround it with quotes\" to give it as one argument",
                args[2]
            )

            return
        }

        val targetChannel = foundVoiceChannels[0].idLong
        val targetRole = foundRoles[0].idLong

        val cache = vcAutoRoleCache.get(guild.idLong) ?: vcAutoRoleCache.put(guild.idLong, TLongLongHashMap())

        cache.put(targetChannel, targetRole)
        ctx.databaseAdapter.setVcAutoRole(guild.idLong, targetChannel, targetRole)

        sendMsg(event, "Role <@&$targetRole> will now be applied to a user when they join <#$targetChannel>")
    }

    private fun listAutoVcRoles(ctx: CommandContext) {

        val items = ctx.variables.vcAutoRoleCache.get(ctx.guild.idLong)

        val embed = EmbedUtils.defaultEmbed()
            .setDescription("List of vc auto roles:\n")

        items.forEachEntry { vc, role ->

            embed.appendDescription("<#$vc> => <@&$role>\n")

            return@forEachEntry true
        }

        sendEmbed(ctx.event, embed)
    }
}
