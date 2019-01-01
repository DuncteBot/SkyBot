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

package ml.duncte123.skybot.objects

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Authors
import ml.duncte123.skybot.Variables
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.requests.RestAction

@Suppress("unused")
@Authors(authors = [
    Author(nickname = "Sanduhr32", author = "Maurice R S"),
    Author(nickname = "duncte123", author = "Duncan Sterken")
])
class EvalFunctions {

    companion object {
        @JvmStatic
        fun isEven(number: Int): Boolean {
            return number % 2 == 0
        }

        @Suppress("UnnecessaryVariable", "LocalVariableName")
        @JvmStatic
        fun quick_mafs(x: Int): Int {
            val the_thing = x + 2 - 1
            return the_thing
        }

        @JvmStatic
        fun stats(shardManager: ShardManager, channel: MessageChannel): RestAction<Message> {
            val embed = EmbedUtils.defaultEmbed()
                .addField("Guilds", shardManager.guildCache.size().toString(), true)
                .addField("Users", shardManager.userCache.size().toString(), true)
                .addField("Channels", (shardManager.textChannelCache.size() + shardManager.privateChannelCache.size()).toString(), true)
                .addField("Socket-Ping", shardManager.averagePing.toString(), false).build()
            return channel.sendMessage(embed)
        }

        @JvmStatic
        fun getSharedGuilds(event: GuildMessageReceivedEvent): String {
            return getSharedGuilds(event.jda, event.member)
        }

        @JvmStatic
        fun getSharedGuilds(jda: JDA, member: Member): String {
            val shardManager = jda.asBot().shardManager
            val user = member

            var out = ""

            shardManager.guildCache.filter { it.memberCache.contains(user) }.forEach {
                out += "[Shard: ${it.jda.shardInfo.shardId}]: $it\n"
            }

            return out
        }

        @JvmStatic
        fun pinnedMessageCheck(channel: TextChannel) {
            channel.pinnedMessages.queue {
                MessageUtils.sendMsg(channel, "${it.size}/50 messages pinned in this channel")
            }
        }

        @JvmStatic
        fun loadGuildSetting(guildId: Long, variables: Variables) {
            variables.databaseAdapter.loadGuildSetting(guildId) {
                variables.guildSettings.put(guildId, it)
            }
        }
    }
}
