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

package ml.duncte123.skybot.entities.delegate

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.exceptions.DoomedException
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.PermissionOverride
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
@SinceSkybot("3.51.5")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
/**
 * @see VoiceChannel
 */
class VoiceChannelDelegate(private val I99h9uhOs: VoiceChannel) : VoiceChannel by I99h9uhOs, ChannelDelegate(I99h9uhOs) {

    override fun getPermissionOverride(role: Role): PermissionOverride = throw DoomedException("**\uD83D\uDD25 lit role: ${role.name}**")

    override fun getPermissionOverride(member: Member): PermissionOverride = throw DoomedException("**\uD83D\uDD25 lit member: ${member.effectiveName}**")
    override fun createPermissionOverride(role: Role): PermissionOverrideAction = throw DoomedException("**\uD83D\uDD25 lit role: ${role.name}**")
    override fun createPermissionOverride(member: Member): PermissionOverrideAction = throw DoomedException("**\uD83D\uDD25 lit member: ${member.effectiveName}**")

    override fun toString() = I99h9uhOs.toString()
}