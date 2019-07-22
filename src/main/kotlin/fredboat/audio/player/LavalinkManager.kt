/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package fredboat.audio.player

import lavalink.client.io.LavalinkRegion
import lavalink.client.io.Link
import lavalink.client.io.jda.JdaLavalink
import lavalink.client.player.IPlayer
import lavalink.client.player.LavaplayerPlayerWrapper
import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.objects.config.DunctebotConfig
import ml.duncte123.skybot.utils.AudioUtils
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.VoiceChannel
import java.net.URI
import java.util.*

/*
 * This class has been taken from
 * https://github.com/Frederikam/FredBoat/blob/master/FredBoat/src/main/java/fredboat/audio/player/LavalinkManager.java
 * and has been modified to fit our needs
 */
object LavalinkManager {

    private lateinit var lavalink: JdaLavalink
    private lateinit var config: DunctebotConfig
    private lateinit var audioUtils: AudioUtils

    fun start(c: DunctebotConfig, a: AudioUtils) {
        config = c
        audioUtils = a

        if (isEnabled()) {
            val userId = String(Base64.getDecoder().decode(c.discord.token.split(".")[0]))
            lavalink = JdaLavalink(
                userId,
                config.discord.totalShards,
                SkyBot.shardManager::getShardById)

            lavalink.nodes.clear()
            for (node in c.lavalink.nodes) {
                lavalink.addNode(Objects.requireNonNull(URI(node.wsurl)), node.pass, LavalinkRegion.valueOf(node.region))
            }
        }
    }

    fun isEnabled() = config.lavalink.enable

    fun createPlayer(guild: Long): IPlayer =
        if (isEnabled())
            lavalink.getLink(guild.toString()).player
        else
            LavaplayerPlayerWrapper(audioUtils.playerManager.createPlayer())

    fun openConnection(channel: VoiceChannel) {
        if (isEnabled()) {
            lavalink.getLink(channel.guild).connect(channel)
        } else {
            channel.guild.audioManager.openAudioConnection(channel)
        }
    }

    fun closeConnection(guild: Guild) {
        if (isEnabled()) {
            lavalink.getLink(guild).disconnect()
        } else {
            guild.audioManager.closeAudioConnection()
        }
    }

    fun isConnected(guild: Guild): Boolean =
        if (isEnabled())
            lavalink.getLink(guild).state == Link.State.CONNECTED
        else
            guild.audioManager.isConnected

    fun getConnectedChannel(guild: Guild): VoiceChannel {
        // Duncte's note:
        // never use the local audio manager, since the audio connection may be remote
        // there is also no reason to look the channel up remotely from lavalink, if we have access to a real guild
        // object here, since we can use the voice state of ourselves (and lavalink 1.x is buggy in keeping up with the
        // current voice channel if the bot is moved around in the client)
        return guild.selfMember.voiceState.channel
    }

    fun getLavalink() = lavalink
}
