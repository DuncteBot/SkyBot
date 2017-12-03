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

package ml.duncte123.skybot.entities

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.DocumentationNeeded
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.entities.delegate.*
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.managers.Presence

@DocumentationNeeded("FUNCTION")
@SinceSkybot("3.51.10")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
public inline fun <reified T> delegateOf(jdaobject: (T)): Any? {
    return when (jdaobject) {
        is Category -> CategoryDelegate(jdaobject)
        is TextChannel -> TextChannelDelegate(jdaobject)
        is VoiceChannel -> VoiceChannelDelegate(jdaobject)
        is Channel -> ChannelDelegate(jdaobject)
        is Guild -> GuildDelegate(jdaobject)
        is JDA -> JDADelegate(jdaobject)
        is Member -> MemberDelegate(jdaobject)
        is Presence -> PresenceDelegate(jdaobject)
        is Role -> RoleDelegate(jdaobject)
        is User -> UserDelegate(jdaobject)
        else -> {
            null
        }
    }
}