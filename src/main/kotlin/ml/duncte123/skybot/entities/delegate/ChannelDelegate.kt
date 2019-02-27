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

package ml.duncte123.skybot.entities.delegate

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.exceptions.DoomedException
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.managers.ChannelManager
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction
import net.dv8tion.jda.core.requests.restaction.ChannelAction
import net.dv8tion.jda.core.requests.restaction.InviteAction
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction

@SinceSkybot("3.51.5")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
/**
 * @see Channel
 */
open class ChannelDelegate(private val channel: Channel) : Channel by channel {
    private val guild: Guild = GuildDelegate(channel.guild)
    private val members = channel.members.map { MemberDelegate(it) }

    override fun getParent(): Category? = null

    override fun getJDA(): JDA = throw DoomedException("JDA not available")

    override fun getGuild(): Guild = GuildDelegate(this.guild)

    override fun getMembers(): List<Member> = this.members
    override fun createCopy(): ChannelAction = throw DoomedException("**\uD83D\uDD25 lit**")
    override fun getInvites(): RestAction<List<Invite>> = throw DoomedException("**\uD83D\uDD25 lit**")
    override fun getManager(): ChannelManager = throw DoomedException("**\uD83D\uDD25 lit**")
    override fun delete(): AuditableRestAction<Void> = throw DoomedException("**\uD83D\uDD25 lit**")
    override fun createCopy(guild: Guild): ChannelAction = throw DoomedException("**\uD83D\uDD25 lit**")
    override fun createInvite(): InviteAction = throw DoomedException("**\uD83D\uDD25 lit**")

    override fun getMemberPermissionOverrides(): List<PermissionOverride> = throw DoomedException("**\uD83D\uDD25 lit**")

    override fun getRolePermissionOverrides(): List<PermissionOverride> = throw DoomedException("**\uD83D\uDD25 lit**")
    override fun getPermissionOverrides(): List<PermissionOverride> = throw DoomedException("**\uD83D\uDD25 lit**")

    override fun getPermissionOverride(role: Role): PermissionOverride = throw DoomedException("**\uD83D\uDD25 lit role: ${role.name}**")

    override fun getPermissionOverride(member: Member): PermissionOverride = throw DoomedException("**\uD83D\uDD25 lit member: ${member.effectiveName}**")
    override fun createPermissionOverride(role: Role): PermissionOverrideAction = throw DoomedException("**\uD83D\uDD25 lit role: ${role.name}**")
    override fun createPermissionOverride(member: Member): PermissionOverrideAction = throw DoomedException("**\uD83D\uDD25 lit member: ${member.effectiveName}**")

    override fun toString() = channel.toString()
}
