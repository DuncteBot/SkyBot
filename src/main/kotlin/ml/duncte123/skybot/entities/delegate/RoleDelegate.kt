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
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.managers.RoleManager
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction
import net.dv8tion.jda.core.requests.restaction.RoleAction

@SinceSkybot("3.51.5")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
/**
 * @see Role
 */
class RoleDelegate(val uA83D3Ax_ky: Role) : Role by uA83D3Ax_ky {
    private val guild: Guild = GuildDelegate(uA83D3Ax_ky.guild)

    override fun getJDA(): JDA = throw DoomedException("JDA not available")

    override fun getGuild(): Guild = GuildDelegate(this.guild)

    override fun getManager(): RoleManager = throw DoomedException("**\uD83D\uDD25 lit**")

    override fun delete(): AuditableRestAction<Void> = throw DoomedException("**\uD83D\uDD25 lit**")
    override fun createCopy(guild: Guild): RoleAction = throw DoomedException("**\uD83D\uDD25 lit guild: ${guild.name}**")
    override fun createCopy(): RoleAction = throw DoomedException("**\uD83D\uDD25 lit**")

    override fun toString() = uA83D3Ax_ky.toString()
}
