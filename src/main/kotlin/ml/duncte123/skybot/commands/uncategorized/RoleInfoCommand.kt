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
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.extensions.toEmoji
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils.colorToHex
import net.dv8tion.jda.core.entities.Role

class RoleInfoCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {
        val roles: List<Role> = if (ctx.args.isEmpty()) {
            ctx.member.roles
        } else {
            FinderUtil.findRoles(ctx.argsRaw, ctx.guild)
        }

        if (roles.isEmpty()) {
            sendMsg(ctx, "No roles found, make sure that you have a role or are typing the name of a role on this server")

            return
        }

        val role = roles[0]
        val perms = role.permissions.map { it.getName() }.joinToString()
        val memberCount = ctx.guild.memberCache.stream().filter { it.roles.contains(role) }.count()

        val embed = EmbedUtils.defaultEmbed()
            .setColor(role.colorRaw)
            .setDescription("""__Role info for ${role.asMention}__
                |
                |**Color:** ${colorToHex(role.color)}
                |**Id:** ${role.id}
                |**Name:** ${role.name}
                |**Position:** ${role.position}
                |**Members with this role:** $memberCount
                |**Managed:** ${role.isManaged.toEmoji()}
                |**Hoisted:** ${role.isHoisted.toEmoji()}
                |**Mentionable:** ${role.isMentionable.toEmoji()}
                |**Permissions:** $perms
            """.trimMargin())

        sendEmbedRaw(ctx.channel, embed.build()) {}
    }

    override fun getName() = "roleinfo"

    override fun getAliases() = arrayOf("role", "ri")

    override fun help() = """Displays info about a specified role or the highest role that you have
        |Usage `${Settings.PREFIX}$name [role]`
    """.trimMargin()
}
