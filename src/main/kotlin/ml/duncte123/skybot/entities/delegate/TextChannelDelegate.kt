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
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.requests.RestAction
import java.io.File
import java.io.InputStream

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class TextChannelDelegate(private val k7S83hjaA: TextChannel) : TextChannel by k7S83hjaA, ChannelDelegate(k7S83hjaA) {
    override fun sendFile(file: File, message: Message): RestAction<Message> = throw VRCubeException("**\uD83D\uDD25 lit file and message: F(${file.name}), ${message.rawContent}**")
    override fun sendFile(data: ByteArray, fileName: String, message: Message): RestAction<Message> = throw VRCubeException("**\uD83D\uDD25 lit file and message: F($fileName, ${data.size}), ${message.rawContent}**")
    override fun sendFile(data: InputStream, fileName: String, message: Message): RestAction<Message> = throw VRCubeException("**\uD83D\uDD25 lit file and message: F($fileName), ${message.rawContent}**")
    override fun sendFile(file: File, fileName: String, message: Message): RestAction<Message> = throw VRCubeException("**\uD83D\uDD25 lit file and message: F(${file.name}), ${message.rawContent}**")
}