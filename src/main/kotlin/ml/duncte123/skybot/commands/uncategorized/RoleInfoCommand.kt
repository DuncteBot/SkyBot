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

package ml.duncte123.skybot.commands.uncategorized

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbedRaw
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.extensions.parseTimes
import ml.duncte123.skybot.extensions.toEmoji
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils.colorToHex
import net.dv8tion.jda.api.entities.Role

class RoleInfoCommand : Command() {

    init {
        this.name = "roleinfo"
        this.aliases = arrayOf("role", "ri")
        this.helpFunction = { _, _ -> "Displays info about a specified role or the highest role that you have" }
        this.usageInstructions = { prefix, invoke -> "`$prefix$invoke [@role]`" }
    }

    override fun execute(ctx: CommandContext) {
        // Obtain the list of target roles
        // If the arguments are empty we will use the roles that the member has
        // otherwise we will pick the one that is mentioned
        val roles: List<Role> = if (ctx.args.isEmpty()) {
            ctx.member.roles
        } else {
            FinderUtil.findRoles(ctx.argsRaw, ctx.jdaGuild)
        }

        // If there are no roles found we need to send an error message with a small hint
        if (roles.isEmpty()) {
            sendMsg(ctx, """No roles found, make sure that you have a role or are typing the name of a role on this server
                |Hint: you cna use `${ctx.prefix}roles` to get a list of the roles in this server
            """.trimMargin())

            return
        }

        // In order: get the highest role
        // Map the permissions to a readable string
        // Get the amount of members with this role
        // Get the creation times of this role
        val role = roles[0]
        val perms = role.permissions.joinToString { it.getName() }
        val memberCount = ctx.jdaGuild.memberCache.applyStream { it.filter { r -> r.roles.contains(role) }.count() }
        val times = ctx.variables.prettyTime.parseTimes(role)

        val embed = EmbedUtils.defaultEmbed()
            .setColor(role.colorRaw)
            .setDescription("""__Role info for ${role.asMention}__
                |
                |**Color:** ${colorToHex(role.colorRaw)}
                |**Id:** ${role.id}
                |**Name:** ${role.name}
                |**Created:** ${times.first} (${times.second})
                |**Position:** ${role.position}
                |**Members with this role:** $memberCount
                |**Managed:** ${role.isManaged.toEmoji()}
                |**Hoisted:** ${role.isHoisted.toEmoji()}
                |**Mentionable:** ${role.isMentionable.toEmoji()}
                |**Permissions:** $perms
            """.trimMargin())

        sendEmbedRaw(ctx.channel, embed.build()) {}
    }
}
