/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.SinceSkybot
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.managers.RoleManager
import net.dv8tion.jda.core.managers.RoleManagerUpdatable
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction
import net.dv8tion.jda.core.requests.restaction.RoleAction

@SinceSkybot("3.51.5")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
/**
 * @see Role
 */
class RoleDelegate(private val uA83D3Ax_ky: Role) : Role by uA83D3Ax_ky {

    private val jda: JDA     = JDADelegate(uA83D3Ax_ky.jda)
    private val guild: Guild = GuildDelegate(uA83D3Ax_ky.guild)
    /**
     * @returns a never null [JDA] ([JDADelegate])
     */
    override fun getJDA(): JDA                               = JDADelegate(this.jda)
    /**
     * @returns a never null [Guild] ([GuildDelegate])
     */
    override fun getGuild(): Guild                           = GuildDelegate(this.guild)

    /**
     * This documentation is for the following four functions.
     *
     * @throws VRCubeException always a [VRCubeException] with the message "**🔥lit type: type.name**"
     */
    override fun getManager(): RoleManager                   = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun getManagerUpdatable(): RoleManagerUpdatable = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun delete(): AuditableRestAction<Void>         = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun createCopy(guild: Guild): RoleAction        = throw VRCubeException("**\uD83D\uDD25 lit guild: ${guild.name}**")
    override fun createCopy(): RoleAction                    = throw VRCubeException("**\uD83D\uDD25 lit**")
}