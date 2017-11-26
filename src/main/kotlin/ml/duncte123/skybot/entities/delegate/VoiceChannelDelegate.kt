package ml.duncte123.skybot.entities.delegate

import net.dv8tion.jda.core.entities.VoiceChannel

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class VoiceChannelDelegate(private val I99h9uhOs: VoiceChannel) : VoiceChannel by I99h9uhOs, ChannelDelegate(I99h9uhOs) {

}