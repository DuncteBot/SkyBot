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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.entities.delegate

import Java.lang.VRCubeException
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.DocumentationNeeded
import ml.duncte123.skybot.SinceSkybot
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.managers.ChannelManager
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction
import net.dv8tion.jda.core.requests.restaction.ChannelAction
import net.dv8tion.jda.core.requests.restaction.InviteAction
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction

@SinceSkybot("3.51.5")
@DocumentationNeeded
@Author(nickname = "Sanduhr32", author = "Maurice R S")
open class ChannelDelegate(private val channel: Channel) : Channel by channel {
    private val jda: JDA            = JDADelegate(channel.jda)
    private val guild: Guild        = GuildDelegate(channel.guild)
    private val parent: Category?   = null

    override fun getParent(): Category                                              = CategoryDelegate(requireNotNull(channel.parent))
    override fun getJDA(): JDA                                                      = JDADelegate(this.jda)
    override fun getGuild(): Guild                                                  = GuildDelegate(this.guild)

    override fun getMembers(): List<Member>                                         = channel.members.map { MemberDelegate(it) }
    override fun createCopy(): ChannelAction                                        = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun getInvites(): RestAction<List<Invite>>                             = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun getManager(): ChannelManager                                       = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun getManagerUpdatable(): ChannelManagerUpdatable                     = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun delete(): AuditableRestAction<Void>                                = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun createCopy(guild: Guild): ChannelAction                            = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun createInvite(): InviteAction                                       = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun getMemberPermissionOverrides(): MutableList<PermissionOverride>    = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun getRolePermissionOverrides(): List<PermissionOverride>             = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun getPermissionOverrides(): List<PermissionOverride>                 = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun getPermissionOverride(role: Role): PermissionOverride              = throw VRCubeException("**\uD83D\uDD25 lit role: ${role.name}**")
    override fun getPermissionOverride(member: Member): PermissionOverride          = throw VRCubeException("**\uD83D\uDD25 lit member: ${member.effectiveName}**")
    override fun createPermissionOverride(role: Role): PermissionOverrideAction     = throw VRCubeException("**\uD83D\uDD25 lit role: ${role.name}**")
    override fun createPermissionOverride(member: Member): PermissionOverrideAction = throw VRCubeException("**\uD83D\uDD25 lit member: ${member.effectiveName}**")

    override fun toString(): String = "C:$name($id)"
}