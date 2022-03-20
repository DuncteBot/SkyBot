/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.utils

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.extensions.parseTimeCreated
import ml.duncte123.skybot.extensions.toEmoji
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils.colorToHex
import net.dv8tion.jda.api.entities.Role

class RoleInfoCommand : Command() {

    init {
        this.category = CommandCategory.UTILS
        this.name = "roleinfo"
        this.aliases = arrayOf("role", "ri")
        this.help = "Displays info about a specified role or the highest role that you have"
        this.usage = "[@role]"
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
            sendMsg(
                ctx,
                """No roles found, make sure that you have a role or are typing the name of a role on this server
                |Hint: you can use `${ctx.prefix}roles` to get a list of the roles in this server
                """.trimMargin()
            )

            return
        }

        val role = roles[0]
        val perms = role.permissions.joinToString { it.getName() }
        val memberCount = ctx.jdaGuild.findMembersWithRoles(role).get().size
        val times = role.parseTimeCreated()
        val tags = role.tags
        val botDisp = if (tags.isBot) "\n**Bot:** <@${tags.botIdLong}>" else ""

        val embed = EmbedUtils.getDefaultEmbed()
            .setColor(role.colorRaw)
            .setDescription(
                """__Role info for ${role.asMention}__
                |
                |**Color:** ${colorToHex(role.colorRaw)}
                |**Id:** ${role.id}
                |**Name:** ${role.name}
                |**Created:** ${times.first} (${times.second})
                |**Position:** ${role.position}
                |**Members with this role:** $memberCount
                |**Managed:** ${role.isManaged.toEmoji()}
                |**Bot role:** ${tags.isBot.toEmoji()}$botDisp
                |**Boost role:** ${tags.isBoost.toEmoji()}
                |**Integration role:**  ${tags.isIntegration.toEmoji()}
                |**Hoisted:** ${role.isHoisted.toEmoji()}
                |**Mentionable:** ${role.isMentionable.toEmoji()}
                |**Permissions:** $perms
                """.trimMargin()
            )

        sendEmbed(ctx, embed, true)
    }
}
