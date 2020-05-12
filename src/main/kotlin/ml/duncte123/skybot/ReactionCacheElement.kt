/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
data class ReactionCacheElement(val msgID: Long, val authorId: Long) {
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }

        if (other is GuildMessageReceivedEvent) {
            return other.author.idLong == this.authorId
        }

        if (other is ReactionCacheElement) {
            return other.authorId == this.authorId
        }

        return false
    }

    override fun hashCode(): Int {
        var result = msgID.hashCode()
        result = 31 * result + authorId.hashCode()
        return result
    }
}
