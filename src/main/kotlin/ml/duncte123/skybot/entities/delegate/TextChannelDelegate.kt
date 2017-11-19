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