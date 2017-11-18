/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
 *
 */

package ml.duncte123.skybot.entities.delegate

import Java.lang.VRCubeException
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.managers.Presence

class PresenceDelegate(private val presence: Presence) : Presence by presence {
    private val jda: JDA = JDADelegate(presence.jda)

    override fun getJDA(): JDA = JDADelegate(this.jda)
    
    override fun setGame(game: Game) = throw VRCubeException("Can not set the game")
    override fun setIdle(idle: Boolean) = throw VRCubeException("Can not set the idle state")
    override fun setStatus(status: OnlineStatus) = throw VRCubeException("Can not set the online status")
    
    override fun setPresence(game: Game, idle: Boolean) = throw VRCubeException("Can not set the game and idle state")
    override fun setPresence(status: OnlineStatus, game: Game) = throw VRCubeException("Can not set the game and online status")
    override fun setPresence(status: OnlineStatus, idle: Boolean) = throw VRCubeException("Can not set the online status and idle state")
    override fun setPresence(status: OnlineStatus, game: Game, idle: Boolean) = throw VRCubeException("Can not set the online status, game and idle state")
}
