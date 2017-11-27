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

package ml.duncte123.skybot.commands.essentials.eval.filter

import ml.duncte123.skybot.entities.delegate.*
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.managers.Presence

class KotlinMappingFilter {
    companion object {
        fun transform(o: Any?): Any? {
            if(o == null) return null
            
            if (o is JDA)
                return JDADelegate(o)
            if (o is Guild)
                return GuildDelegate(o)
            if (o is Member)
                return MemberDelegate(o)
            if (o is Presence)
                return PresenceDelegate(o)
            if (o is User)
                return UserDelegate(o)
        
            return o
        }
    }
}