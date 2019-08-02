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
import gnu.trove.map.TLongLongMap
import gnu.trove.map.hash.TLongLongHashMap
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.VoiceChannel
import java.util.function.BiFunction
import java.util.stream.Collectors

class VcAutoRoleCommand : ModBaseCommand() {

    init {
        this.category = CommandCategory.ADMINISTRATION
        this.name = "vcautorole"
        this.helpFunction = BiFunction { _, _ -> "Gives a role to a user when they join a specified voice channel" }
        this.usageInstructions = BiFunction { invoke, prefix ->
            """`$prefix$invoke add <voice channel> <@role>`
        |`$prefix$invoke remove <voice channel>`
        |`$prefix$invoke off`
        |`$prefix$invoke list`
        """.trimMargin()
        }
        this.userPermissions = arrayOf(Permission.MANAGE_SERVER)
        this.botPermissions = arrayOf(Permission.MANAGE_SERVER, Permission.MANAGE_ROLES)
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
                sendMsg(event, "Missing arguments, check `${ctx.prefix}help $name`")
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

        sendMsg(event, "Unknown operation, check `${ctx.prefix}$name`")

    }

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

        val foundRoles = FinderUtil.findRoles(args[2], guild)

        if (foundRoles.isEmpty()) {
            sendMsgFormat(
                event,
                "I could not find any role for `%s`%n" +
                    "TIP: If your role name has spaces \"Surround it with quotes\" to give it as one argument",
                args[2]
            )

            return
        }

        val targetRole = foundRoles[0].idLong
        var cache: TLongLongMap? = vcAutoRoleCache.get(guild.idLong)

        if (cache == null) {
            val new = TLongLongHashMap()

            vcAutoRoleCache.put(guild.idLong, new)
            cache = new
        }

        if (args[1].toLowerCase() == "all") {
            val ids = guild.voiceChannelCache.stream().map(VoiceChannel::getIdLong).collect(Collectors.toList())

            ctx.databaseAdapter.setVcAutoRoleBatch(guild.idLong, ids, targetRole)
            ids.forEach { cache.put(it, targetRole) }

            sendMsg(event, "Role <@&$targetRole> will now be applied to a user when they join any voice channel " +
                "(excluding ones that are created after the command was ran)")
        } else {
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

            cache.put(targetChannel, targetRole)
            ctx.databaseAdapter.setVcAutoRole(guild.idLong, targetChannel, targetRole)

            sendMsg(event, "Role <@&$targetRole> will now be applied to a user when they join <#$targetChannel>")
        }
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
