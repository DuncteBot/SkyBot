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

package ml.duncte123.skybot.utils

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.DocumentationNeeded
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.audio.TrackScheduler
import ml.duncte123.skybot.entities.delegate.*
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.managers.Presence
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.OffsetDateTime

@SinceSkybot("3.51.5")
@DocumentationNeeded
@Author(nickname = "Sanduhr32", author = "Maurice R S")
class EarthUtils {
    companion object {
        @JvmStatic
        fun throwableToJSONObject(throwable: Throwable): JSONObject {
            return JSONObject().put("className", throwable::class.java.name)
                    .put("message", throwable.message)
                    .put("localiziedMessage", throwable.localizedMessage)
                    .put("cause", throwable.cause?.let { throwableToJSONObject(it) })
                    .put("supressed", throwableArrayToJSONArray(throwable.suppressed))
                    .put("stacktraces", stacktraceArrayToJSONArray(throwable.stackTrace))
        }

        @JvmStatic
        private fun throwableArrayToJSONArray(throwables: Array<Throwable>) =
                JSONArray(throwables.map { throwableToJSONObject(it) })

        @JvmStatic
        private fun stacktraceArrayToJSONArray(stackTraces: Array<StackTraceElement>): JSONArray =
                JSONArray(stackTraces.map { stackTraceToJSONObject(it) })

        @JvmStatic
        private fun stackTraceToJSONObject(stackTraceElement: StackTraceElement) =
                JSONObject().put("className", stackTraceElement.className)
                        .put("methodName", stackTraceElement.methodName)
                        .put("lineNumber", stackTraceElement.lineNumber)
                        .put("isNative", stackTraceElement.isNativeMethod)

        @JvmStatic
        fun write(a_file: String, content: String) {
            val file = File(a_file)

            if (!file.exists())
                file.createNewFile()

            FileOutputStream(file).write(content.toByteArray())
        }

        @JvmStatic
        fun delegateOf(jdaobject: Any?): Any? {
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

        @JvmStatic
        fun audioJSON(): JSONObject {
            val json = JSONObject().put("time", OffsetDateTime.now())
            AirUtils.audioUtils.musicManagers.entries.forEach { json.put(it.key, JSONObject().put("guildId", it.key).put("manager", gMMtoJSON(it.value))) }
            return json
        }

        @JvmStatic
        private fun gMMtoJSON(manager: GuildMusicManager): JSONObject =
                JSONObject().put("player", playerToJSON(manager.player)).put("scheduler", schedulerToJSO(manager.scheduler))

        @JvmStatic
        private fun playerToJSON(player: AudioPlayer): JSONObject =
                JSONObject().put("currentTrack", player.playingTrack?.let { trackToJSON(it) }).put("paused",player.isPaused)
                        .put("volume", player.volume)

        @JvmStatic
        private fun schedulerToJSO(scheduler: TrackScheduler): JSONObject =
                JSONObject().put("repeating", scheduler.isRepeating).put("queue_size", scheduler.queue.size)

        @JvmStatic
        private fun trackToJSON(track: AudioTrack): JSONObject =
                JSONObject().put("source", track.sourceManager.sourceName).put("position", track.position)
                    .put("stream",track.info.isStream).put("uri", track.info.uri).put("length", track.info.length)
                    .put("title", track.info.title)
    }
}