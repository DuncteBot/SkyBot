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

package ml.duncte123.skybot.commands.music

import com.dunctebot.sourcemanagers.pornhub.PornHubAudioSourceManager
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.YoutubeUtils.searchYoutubeIdOnly

@Author(nickname = "Sanduhr32", author = "Maurice R S")
open class PlayCommand(private val skipParsing: Boolean = false) : MusicCommand() {

    init {
        this.withAutoJoin = true
        this.name = "play"
        this.help = "Plays a song on the bot or adds it to the queue"
        this.usage = "[url/search term]"
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val guild = event.guild
        val mng = getMusicManager(guild, ctx.audioUtils)
        val player = mng.player
        val scheduler = mng.scheduler

        if (ctx.args.isEmpty()) {
            when {
                player.isPaused -> {
                    player.isPaused = false

                    sendMsg(ctx, "Playback has been resumed.")
                }

                player.playingTrack != null -> sendMsg(ctx, "Player is already playing!")

                scheduler.queue.isEmpty() -> sendMsg(ctx, "The current audio queue is empty! Add something to the queue first!\n" +
                    "For example `${ctx.prefix}play https://www.youtube.com/watch?v=KKOBXrRzZwA`")
            }

            return
        }

        var toPlay = ctx.argsRaw

        if (toPlay.contains(PornHubAudioSourceManager.DOMAIN_REGEX.toRegex()) && !ctx.channel.isNSFW) {
            sendMsg(ctx, "Because of thumbnails being loaded you can only use PornHub links in channels that are marked as NSFW")
            return
        }

        if (skipParsing) {
            handlePlay(toPlay, ctx, mng)
            return
        }

        if (!AirUtils.isURL(toPlay)) {
            val vidId = searchCache(toPlay, ctx)

            if (vidId == null) {
                MessageUtils.sendError(event.message)
                sendMsg(ctx, "No tracks where found")
                return
            }
            toPlay = "https://www.youtube.com/watch?v=${vidId}"
        }

        handlePlay(toPlay, ctx, mng)
    }

    private fun searchCache(search: String, ctx: CommandContext): String? {
        val res = searchYoutubeIdOnly(search, ctx.config.apis.googl, 1L)

        if(res.isEmpty()) {
            return null
        }

        return res[0].id.videoId
    }

    private fun handlePlay(toPlay: String, ctx: CommandContext, mng: GuildMusicManager?) {
        if (toPlay.length > 1024) {
            MessageUtils.sendError(ctx.message)
            sendMsg(ctx, "Input cannot be longer than 1024 characters.")
            return
        }

        ctx.audioUtils.loadAndPlay(mng, toPlay, ctx)
    }
}
