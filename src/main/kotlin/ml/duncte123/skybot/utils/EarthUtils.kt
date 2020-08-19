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

package ml.duncte123.skybot.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gnu.trove.map.TLongIntMap
import lavalink.client.player.IPlayer
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.EmbedUtils.getDefaultEmbed
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.audio.TrackScheduler
import ml.duncte123.skybot.extensions.sync
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import java.time.OffsetDateTime
import java.util.concurrent.ThreadLocalRandom

@SinceSkybot("3.51.5")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
object EarthUtils {
    @JvmStatic
    fun sendYoungestOldesetEmbed(ctx: CommandContext, oldest: Boolean) {
        val user = findOldestOrYoungestUser(ctx.jdaGuild, oldest)
        val embed = EmbedUtils.embedMessage("""The ${if (oldest) "oldest" else "youngest"} member in this server is:
            |${user.asTag} - ${user.asMention}
        """.trimMargin())
            .setFooter("Account created")
            .setTimestamp(user.timeCreated)

        sendEmbed(ctx, embed)
    }

    @JvmStatic
    fun findOldestOrYoungestUser(guild: Guild, oldest: Boolean): User {
        val members = guild.loadMembers().sync()
        val index = if (oldest) 0 else members.size - 1

        return members.map(Member::getUser).sortedBy(User::getTimeCreated)[index]
    }

    /**
     *
     * This function generates a debug JSON that can help us to improve errors if we hide them.
     *
     * @param throwable a [Throwable] that provides data.
     * @returns a [JsonNode] that contains all given details.
     *
     *
     * @see [EarthUtils.throwableArrayToJSONArray]
     * @see [EarthUtils.stacktraceArrayToJSONArray]
     * @see [EarthUtils.stackTraceToJSONObject]
     */
    @JvmStatic
    fun throwableToJSONObject(throwable: Throwable, mapper: ObjectMapper): JsonNode {
        val node = mapper.createObjectNode()
            .put("className", throwable::class.java.name)
            .put("message", throwable.message)
            .put("localiziedMessage", throwable.localizedMessage)

        node.set<ArrayNode>("stacktraces", stacktraceArrayToJSONArray(throwable.stackTrace, mapper))
        node.set<ArrayNode>("supressed", throwableArrayToJSONArray(throwable.suppressed, mapper))
        node.set<JsonNode>("cause", throwable.cause?.let { throwableToJSONObject(it, mapper) })

        return node
    }

    /**
     * This small function wraps [List]<[Throwable]> into an [ArrayNode]
     *
     *
     * @see [EarthUtils.throwableToJSONObject]
     * @see [EarthUtils.stacktraceArrayToJSONArray]
     * @see [EarthUtils.stackTraceToJSONObject]
     */
    @JvmStatic
    private fun throwableArrayToJSONArray(throwables: Array<Throwable>, mapper: ObjectMapper): ArrayNode {
        val array = mapper.createArrayNode()

        array.addAll(throwables.map { throwableToJSONObject(it, mapper) })

        return array
    }

    /**
     * This tiny function wraps [List]<[StackTraceElement]> into an [ArrayNode]
     *
     *
     * @see [EarthUtils.throwableToJSONObject]
     * @see [EarthUtils.stackTraceToJSONObject]
     */
    @JvmStatic
    private fun stacktraceArrayToJSONArray(stackTraces: Array<StackTraceElement>, mapper: ObjectMapper): ArrayNode {
        val array = mapper.createArrayNode()

        array.addAll(stackTraces.map { stackTraceToJSONObject(it, mapper) })

        return array
    }

    /**
     * This is just a smaller function that converts [StackTraceElement]s into [ObjectNode] that we use in the see tag
     *
     *
     * @see [EarthUtils.throwableToJSONObject]
     * @see [EarthUtils.stacktraceArrayToJSONArray]
     */
    @JvmStatic
    private fun stackTraceToJSONObject(stackTraceElement: StackTraceElement, mapper: ObjectMapper): ObjectNode {
        return mapper.createObjectNode()
            .put("className", stackTraceElement.className)
            .put("methodName", stackTraceElement.methodName)
            .put("lineNumber", stackTraceElement.lineNumber)
            .put("isNative", stackTraceElement.isNativeMethod)
    }

    /**
     *
     * This function generates a debug JSON that can help us to improve audio and memory issues.
     *
     * @returns a [ObjectNode] that contains all given details.
     *
     *
     * @see [EarthUtils.gMMtoJSON]
     * @see [EarthUtils.playerToJSON]
     * @see [EarthUtils.schedulerToJSON]
     * @see [EarthUtils.trackToJSON]
     */
    @JvmStatic
    fun audioJSON(audioUtils: AudioUtils, mapper: ObjectMapper): ObjectNode {
        val json = mapper.createObjectNode().put("time", OffsetDateTime.now().toString())

        audioUtils.musicManagers.forEachEntry { key, value ->
            json.set<ObjectNode>(key.toString(),
                mapper.createObjectNode()
                    .put("guildId", key)
                    .set("manager", gMMtoJSON(value, mapper))
            )

            return@forEachEntry true
        }
        return json
    }

    /**
     * This tiny function converts a [GuildMusicManager] into a [ObjectNode]
     *
     * @param manager a [GuildMusicManager] that provides data.
     * @returns a [ObjectNode] with all the converted data.
     *
     *
     * @see [EarthUtils.audioJSON]
     * @see [EarthUtils.playerToJSON]
     * @see [EarthUtils.schedulerToJSON]
     * @see [EarthUtils.trackToJSON]
     */
    @JvmStatic
    private fun gMMtoJSON(manager: GuildMusicManager, mapper: ObjectMapper): ObjectNode {
        val node = mapper.createObjectNode()

        node.set<ObjectNode>("scheduler", schedulerToJSON(manager.scheduler, mapper))
        node.set<ObjectNode>("fredboat/audio/player", playerToJSON(manager.player, mapper))

        return node
    }

    /**
     * This is a little function that converts a [AudioPlayer] into a [ObjectNode]
     *
     * @param player a [AudioPlayer] that provides data.
     * @returns a [ObjectNode] with all the converted data.
     *
     *
     * @see [EarthUtils.audioJSON]
     * @see [EarthUtils.gMMtoJSON]
     * @see [EarthUtils.schedulerToJSON]
     * @see [EarthUtils.trackToJSON]
     */
    @JvmStatic
    private fun playerToJSON(player: IPlayer, mapper: ObjectMapper): ObjectNode {
        return mapper.createObjectNode()
            .put("paused", player.isPaused)
            .put("volume", player.volume)
            .set("currentTrack", player.playingTrack?.let { trackToJSON(it, mapper) }) as ObjectNode
    }

    /**
     * This smaller function converts a [TrackScheduler] into a [ObjectNode]
     *
     * @param scheduler a [TrackScheduler] that provides data.
     * @returns a [ObjectNode] with all the converted data.
     *
     *
     * @see [EarthUtils.audioJSON]
     * @see [EarthUtils.gMMtoJSON]
     * @see [EarthUtils.playerToJSON]
     * @see [EarthUtils.trackToJSON]
     */
    @JvmStatic
    private fun schedulerToJSON(scheduler: TrackScheduler, mapper: ObjectMapper): ObjectNode {
        return mapper.createObjectNode()
            .put("repeating", scheduler.isRepeating)
            .put("queue_size", scheduler.queue.size)
    }

    /**
     * This small function that converts a [AudioTrack] into a [ObjectNode]
     *
     * @param track a [AudioTrack] that provides data.
     * @returns a [ObjectNode] with all the converted data.
     *
     *
     * @see [EarthUtils.audioJSON]
     * @see [EarthUtils.gMMtoJSON]
     * @see [EarthUtils.playerToJSON]
     * @see [EarthUtils.schedulerToJSON]
     */
    @JvmStatic
    private fun trackToJSON(track: AudioTrack, mapper: ObjectMapper): ObjectNode {
        return mapper.createObjectNode()
            .put("source", track.sourceManager.sourceName)
            .put("position", track.position)
            .put("stream", track.info.isStream)
            .put("uri", track.info.uri)
            .put("length", track.info.length)
            .put("title", track.info.title)
    }

    @JvmStatic
    fun sendRedditPost(reddit: String, index: TLongIntMap, ctx: CommandContext, all: Boolean = false) {
        val sort = if (all) "/.json?sort=all&t=day&limit=400" else "top/.json?sort=top&t=day&limit=400"

        WebUtils.ins.getJSONObject("https://www.reddit.com/r/$reddit/$sort").async {
            val posts = it["data"]["children"].filter { filter ->
                ctx.channel.isNSFW || !filter["data"]["over_18"].asBoolean()
            }.filter { filter ->
                filter["data"]["selftext"].asText().length <= 550
                    && filter["data"]["title"].asText().length <= 256
            }

            if (posts.isEmpty()) {
                sendError(ctx.message)
                sendMsg(ctx, """Whoops I could not find any posts.
                    |This may be because Reddit is down or all posts are NSFW (NSFW posts are not displayed in channels that are not marked as NSFW)""".trimMargin())
                return@async
            }

            val guildId = ctx.guild.idLong

            // We don't need to check for a contains because default value will be 0
            if (index[guildId] >= posts.size) {
                index.put(guildId, 0)
            }

            val postI = index[guildId]
            var rand = ThreadLocalRandom.current().nextInt(0, posts.size)

            if (postI == rand) {
                rand = ThreadLocalRandom.current().nextInt(0, posts.size)
            }

            val post = posts[rand]["data"]

            index.put(guildId, rand)

            val title: String = post["title"].asText()
            val text: String = post["selftext"].asText("")
            val url: String = post["id"].asText()
            val embed = getDefaultEmbed().setTitle(title, "https://redd.it/$url")

            if (text.isNotEmpty()) {
                embed.setDescription(text)
            }

            if (post.has("preview")) {
                val imagesO = post["preview"]
                val images = imagesO["images"]

                if (images != null) {
                    val image = images[0]["source"]["url"].asText()
                    embed.setImage(image.replaceFirst("preview", "i"))
                }
            }

            sendEmbed(ctx, embed)
        }

    }
}
