/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
@file:Suppress("USELESS_CAST")

package ml.duncte123.skybot.entities.delegate

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.exceptions.DoomedException
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.managers.GuildController
import net.dv8tion.jda.core.managers.GuildManager
import net.dv8tion.jda.core.requests.RestAction

@Suppress("unused")
@SinceSkybot("3.51.5")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
/**
 * @see Guild
 */
class GuildDelegate(private val z88Am1Alk: Guild) : Guild by z88Am1Alk {
    private val jda: JDA = JDADelegate(z88Am1Alk.jda)
    private val manager: GuildManager? = null

    override fun getJDA(): JDA = JDADelegate(this.jda)
    override fun getManager(): GuildManager = throw DoomedException("**\uD83D\uDD25 lit**")

    override fun getMember(user: User): Member = MemberDelegate(z88Am1Alk.getMember(user))
    override fun getSelfMember(): Member = MemberDelegate(z88Am1Alk.selfMember)
    override fun getRoleById(id: Long): Role = RoleDelegate(z88Am1Alk.getRoleById(id))
    override fun getRoleById(id: String): Role = RoleDelegate(z88Am1Alk.getRoleById(id))
    override fun getMemberById(userId: Long): Member = MemberDelegate(z88Am1Alk.getMemberById(userId))
    override fun getMemberById(userId: String): Member = MemberDelegate(z88Am1Alk.getMemberById(userId))

    override fun getMembers(): List<Member> = z88Am1Alk.members.map { MemberDelegate(it) } as List<Member>
    override fun getMembersByEffectiveName(name: String, ignoreCase: Boolean): List<Member> = z88Am1Alk.getMembersByEffectiveName(name, ignoreCase).map { MemberDelegate(it) } as List<Member>
    override fun getMembersByName(name: String, ignoreCase: Boolean): List<Member> = z88Am1Alk.getMembersByName(name, ignoreCase).map { MemberDelegate(it) } as List<Member>
    override fun getMembersByNickname(nickname: String, ignoreCase: Boolean): List<Member> = z88Am1Alk.getMembersByNickname(nickname, ignoreCase).map { MemberDelegate(it) } as List<Member>
    override fun getMembersWithRoles(vararg roles: Role): List<Member> = z88Am1Alk.getMembersWithRoles(*roles).map { MemberDelegate(it) } as List<Member>
    override fun getMembersWithRoles(roles: Collection<Role>): List<Member> = z88Am1Alk.getMembersWithRoles(roles).map { MemberDelegate(it) } as List<Member>
    override fun getRoles(): List<Role> = z88Am1Alk.roles.map { RoleDelegate(it) } as List<Role>
    override fun getRolesByName(name: String, ignoreCase: Boolean): List<Role> = z88Am1Alk.getRolesByName(name, ignoreCase).map { RoleDelegate(it) } as List<Role>

    override fun getController(): GuildController = throw DoomedException("**\uD83D\uDD25 lit**")
    override fun leave(): RestAction<Void> = throw DoomedException("**\uD83D\uDD25 lit**")

    override fun toString() = z88Am1Alk.toString()
}