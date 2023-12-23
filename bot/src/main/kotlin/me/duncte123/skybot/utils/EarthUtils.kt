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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot.utils

import com.github.natanbc.reliqua.limiter.RateLimiter
import gnu.trove.map.TLongIntMap
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.EmbedUtils.getDefaultEmbed
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.skybot.extensions.sync
import me.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.ThreadLocalRandom

object EarthUtils {
    @JvmStatic
    fun sendYoungestOldesetEmbed(ctx: CommandContext, oldest: Boolean) {
        val user = findOldestOrYoungestUser(ctx.jdaGuild, oldest)
        val embed = EmbedUtils.embedMessage(
            """The ${if (oldest) "oldest" else "youngest"} member in this server is:
            |${user.asTag} - ${user.asMention}
            """.trimMargin()
        )
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

    @JvmStatic
    fun sendRedditPost(reddit: String, index: TLongIntMap, ctx: CommandContext, all: Boolean = false) {
        val sort = if (all) "/.json?sort=all&t=day&limit=400" else "top/.json?sort=top&t=day&limit=400"

        WebUtils.ins.getJSONObject("https://www.reddit.com/r/$reddit/$sort") { it.setRateLimiter(RateLimiter.directLimiter()) }
            .async {
                val posts = it["data"]["children"].filter { filter ->
                    ctx.isChannelNSFW || !filter["data"]["over_18"].asBoolean()
                }.filter { filter ->
                    filter["data"]["selftext"].asText().length <= 550 &&
                        filter["data"]["title"].asText().length <= 256
                }

                if (posts.isEmpty()) {
                    sendError(ctx.message)
                    sendMsg(
                        ctx,
                        """Whoops I could not find any posts.
                        |This may be because Reddit is down or all posts are NSFW (NSFW posts are not displayed in channels that are not marked as NSFW)
                        """
                            .trimMargin()
                    )
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
