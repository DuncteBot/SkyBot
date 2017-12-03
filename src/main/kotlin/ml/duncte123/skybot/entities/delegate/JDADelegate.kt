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
 */

package ml.duncte123.skybot.entities.delegate

import Java.lang.VRCubeException
import ml.duncte123.skybot.DocumentationNeeded
import ml.duncte123.skybot.SinceSkybot
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.managers.Presence

@SinceSkybot("3.51.5")
@DocumentationNeeded
class JDADelegate(private val x0sAlsm7sW: JDA) : JDA by x0sAlsm7sW {
    private val token: String      = "Bot MyUltraOPTokenJustForProsAndNotForNoobsTM"
    private val presence: Presence = PresenceDelegate(x0sAlsm7sW.presence)
    
    override fun getToken(): String      = this.token
    override fun getPresence(): Presence = this.presence
    override fun shutdown()              = throw VRCubeException("Can not shutdown")
    override fun shutdownNow()           = this.shutdown()
    
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        
        if (other is JDADelegate)
            return other.x0sAlsm7sW == x0sAlsm7sW
        else if (other is JDA)
            return other == x0sAlsm7sW
        
        return false
    }
    override fun hashCode(): Int {
        var result = x0sAlsm7sW.hashCode()
        result = 32 * result + token.hashCode()
        result = 31 * result + presence.hashCode()
        return result
    }
}
