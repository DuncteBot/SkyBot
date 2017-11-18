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
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.managers.Presence

class PresenceDelegate(private val presence: Presence) : Presence by presence {
    
    override fun setGame(game: Game?) {
        throw VRCubeException("No set game")
    }
    
    override fun setIdle(idle: Boolean) {
        throw VRCubeException("No set idle")
    }
    
    override fun setStatus(status: OnlineStatus?) {
        throw VRCubeException("No set status")
    }
    
    override fun setPresence(game: Game?, idle: Boolean) {
        throw VRCubeException("No set game and idle")
    }
    
    override fun setPresence(status: OnlineStatus?, game: Game?) {
        throw VRCubeException("No set status and game")
    }
    
    override fun setPresence(status: OnlineStatus?, idle: Boolean) {
        throw VRCubeException("No set status and idle")
    }
    
    override fun setPresence(status: OnlineStatus?, game: Game?, idle: Boolean) {
        throw VRCubeException("No set status, game and idle")
    }
}
