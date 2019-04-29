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

package ml.duncte123.skybot.utils

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gnu.trove.map.TLongIntMap
import lavalink.client.player.IPlayer
import me.duncte123.botcommons.messaging.EmbedUtils.defaultEmbed
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.audio.TrackScheduler
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.OffsetDateTime
import java.util.concurrent.ThreadLocalRandom


@SinceSkybot("3.51.5")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
class EarthUtils {
    companion object {

        /**
         *
         * This function generates a debug JSON that can help us to improve errors if we hide them.
         *
         * @param throwable a [Throwable] that provides data.
         * @returns a [JSONObject] that contains all given details.
         *
         *
         * @see [EarthUtils.throwableArrayToJSONArray]
         * @see [EarthUtils.stacktraceArrayToJSONArray]
         * @see [EarthUtils.stackTraceToJSONObject]
         */
        @JvmStatic
        fun throwableToJSONObject(throwable: Throwable): JSONObject {
            return JSONObject().put("className", throwable::class.java.name)
                .put("message", throwable.message)
                .put("localiziedMessage", throwable.localizedMessage)
                .put("cause", throwable.cause?.let { throwableToJSONObject(it) })
                .put("supressed", throwableArrayToJSONArray(throwable.suppressed))
                .put("stacktraces", stacktraceArrayToJSONArray(throwable.stackTrace))
        }

        /**
         * This small function wraps [List]<[JSONObject]> into an [JSONArray]
         *
         *
         * @see [EarthUtils.throwableToJSONObject]
         * @see [EarthUtils.stacktraceArrayToJSONArray]
         * @see [EarthUtils.stackTraceToJSONObject]
         */
        @JvmStatic
        private fun throwableArrayToJSONArray(throwables: Array<Throwable>) =
            JSONArray(throwables.map { throwableToJSONObject(it) })

        /**
         * This tiny function wraps [List]<[JSONObject]> into an [JSONArray]
         *
         *
         * @see [EarthUtils.throwableToJSONObject]
         * @see [EarthUtils.stackTraceToJSONObject]
         */
        @JvmStatic
        private fun stacktraceArrayToJSONArray(stackTraces: Array<StackTraceElement>): JSONArray =
            JSONArray(stackTraces.map { stackTraceToJSONObject(it) })

        /**
         * This is just a smaller function that converts [StackTraceElement]s into [JSONObject] that we use in the see tag
         *
         *
         * @see [EarthUtils.throwableToJSONObject]
         * @see [EarthUtils.stacktraceArrayToJSONArray]
         */
        @JvmStatic
        private fun stackTraceToJSONObject(stackTraceElement: StackTraceElement) =
            JSONObject().put("className", stackTraceElement.className)
                .put("methodName", stackTraceElement.methodName)
                .put("lineNumber", stackTraceElement.lineNumber)
                .put("isNative", stackTraceElement.isNativeMethod)

        @JvmStatic
        @Deprecated(message = "The following code may be removed!", level = DeprecationLevel.WARNING)
        fun write(a_file: String, content: String) {
            val file = File(a_file)

            if (!file.exists()) {
                file.createNewFile()
            }

            FileOutputStream(file).write(content.toByteArray())
        }

        /**
         *
         * This function generates a debug JSON that can help us to improve audio and memory issues.
         *
         * @returns a [JSONObject] that contains all given details.
         *
         *
         * @see [EarthUtils.gMMtoJSON]
         * @see [EarthUtils.playerToJSON]
         * @see [EarthUtils.schedulerToJSON]
         * @see [EarthUtils.trackToJSON]
         */
        @JvmStatic
        fun audioJSON(audioUtils: AudioUtils): JSONObject {
            val json = JSONObject().put("time", OffsetDateTime.now())

            audioUtils.musicManagers.forEachEntry { key, value ->
                json.put(key.toString(), JSONObject().put("guildId", key).put("manager", gMMtoJSON(value)))
                return@forEachEntry true
            }
            return json
        }

        /**
         * This tiny function converts a [GuildMusicManager] into a [JSONObject]
         *
         * @param manager a [GuildMusicManager] that provides data.
         * @returns a [JSONObject] with all the converted data.
         *
         *
         * @see [EarthUtils.audioJSON]
         * @see [EarthUtils.playerToJSON]
         * @see [EarthUtils.schedulerToJSON]
         * @see [EarthUtils.trackToJSON]
         */
        @JvmStatic
        private fun gMMtoJSON(manager: GuildMusicManager): JSONObject =
            JSONObject().put("fredboat/audio/player", playerToJSON(manager.player)).put("scheduler", schedulerToJSON(manager.scheduler))

        /**
         * This is a little function that converts a [AudioPlayer] into a [JSONObject]
         *
         * @param player a [AudioPlayer] that provides data.
         * @returns a [JSONObject] with all the converted data.
         *
         *
         * @see [EarthUtils.audioJSON]
         * @see [EarthUtils.gMMtoJSON]
         * @see [EarthUtils.schedulerToJSON]
         * @see [EarthUtils.trackToJSON]
         */
        @JvmStatic
        private fun playerToJSON(player: IPlayer): JSONObject =
            JSONObject().put("currentTrack", player.playingTrack?.let { trackToJSON(it) }).put("paused", player.isPaused)
                .put("volume", player.volume)

        /**
         * This smaller function converts a [TrackScheduler] into a [JSONObject]
         *
         * @param scheduler a [TrackScheduler] that provides data.
         * @returns a [JSONObject] with all the converted data.
         *
         *
         * @see [EarthUtils.audioJSON]
         * @see [EarthUtils.gMMtoJSON]
         * @see [EarthUtils.playerToJSON]
         * @see [EarthUtils.trackToJSON]
         */
        @JvmStatic
        private fun schedulerToJSON(scheduler: TrackScheduler): JSONObject =
            JSONObject().put("repeating", scheduler.isRepeating).put("queue_size", scheduler.queue.size)

        /**
         * This small function that converts a [AudioTrack] into a [JSONObject]
         *
         * @param track a [AudioTrack] that provides data.
         * @returns a [JSONObject] with all the converted data.
         *
         *
         * @see [EarthUtils.audioJSON]
         * @see [EarthUtils.gMMtoJSON]
         * @see [EarthUtils.playerToJSON]
         * @see [EarthUtils.schedulerToJSON]
         */
        @JvmStatic
        private fun trackToJSON(track: AudioTrack): JSONObject =
            JSONObject().put("source", track.sourceManager.sourceName).put("position", track.position)
                .put("stream", track.info.isStream).put("uri", track.info.uri).put("length", track.info.length)
                .put("title", track.info.title)

        @JvmStatic
        fun sendRedditPost(reddit: String, index: TLongIntMap, event: GuildMessageReceivedEvent, all: Boolean = false) {
            val sort = if (all) "/.json?sort=all&t=day&limit=400" else "top/.json?sort=top&t=day&limit=400"

            WebUtils.ins.getJSONObject("https://www.reddit.com/r/$reddit/$sort").async {
                val posts = it.getJSONObject("data").getJSONArray("children").filter { filter ->
                    event.channel.isNSFW || !(filter as JSONObject).getJSONObject("data").getBoolean("over_18")
                }.filter { filter ->
                    filter as JSONObject

                    filter.getJSONObject("data").getString("selftext").length <= 550
                        && filter.getJSONObject("data").getString("title").length <= 256
                }

                if (posts.isEmpty()) {
                    sendError(event.message)
                    sendMsg(event, """Whoops I could not find any posts.
                    |This may be because Reddit is down or all posts are NSFW (NSFW posts are not displayed in channels that are not marked as NSFW)""".trimMargin())
                    return@async
                }

                // We don't need to check for a contains because default value will be 0
                if (index.get(event.guild.idLong) >= posts.size) {
                    index.put(event.guild.idLong, 0)
                }

                val postI = index.get(event.guild.idLong)
                var rand = ThreadLocalRandom.current().nextInt(0, posts.size)

                if (postI == rand) {
                    rand = ThreadLocalRandom.current().nextInt(0, posts.size)
                }

                val post: JSONObject = JSONArray(posts).getJSONObject(rand).getJSONObject("data")

                index.put(event.guild.idLong, rand)

                val title: String = post.getString("title")
                val text: String = post.optString("selftext", "")
                val url: String = post.getString("id")
                val embed = defaultEmbed().setTitle(title, "https://redd.it/$url")

                if (text.isNotEmpty()) {
                    embed.setDescription(text)
                }

                val imagesO = post.optJSONObject("preview")
                val images = imagesO?.optJSONArray("images")

                if (images != null) {
                    val image = images.getJSONObject(0).getJSONObject("source").getString("url")
                    embed.setImage(image.replaceFirst("preview", "i"))
                }

                sendEmbed(event, embed)
            }

        }
    }
}
