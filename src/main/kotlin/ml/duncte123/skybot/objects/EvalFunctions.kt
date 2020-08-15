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

package ml.duncte123.skybot.objects

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageConfig
import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Authors
import ml.duncte123.skybot.Variables
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.sharding.ShardManager

@Suppress("unused")
@Authors(authors = [
    Author(nickname = "Sanduhr32", author = "Maurice R S"),
    Author(nickname = "duncte123", author = "Duncan Sterken")
])
object EvalFunctions {
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
        val embed = EmbedUtils.getDefaultEmbed()
            .addField("Guilds", shardManager.guildCache.size().toString(), true)
            .addField("Users", shardManager.userCache.size().toString(), true)
            .addField("Channels", (shardManager.textChannelCache.size() + shardManager.privateChannelCache.size()).toString(), true)
            .addField("Socket-Ping", shardManager.averageGatewayPing.toString(), false).build()
        return channel.sendMessage(embed)
    }

    @JvmStatic
    fun getSharedGuilds(event: GuildMessageReceivedEvent): String {
        return getSharedGuilds(event.jda, event.member!!)
    }

    @JvmStatic
    fun getSharedGuilds(jda: JDA, member: Member): String {
        val shardManager = jda.shardManager

        var out = ""

        shardManager!!.getMutualGuilds(member.user).forEach {
            out += "[Shard: ${it.jda.shardInfo.shardId}]: $it\n"
        }

        return out
    }

    @JvmStatic
    fun pinnedMessageCheck(channel: TextChannel) {
        channel.retrievePinnedMessages().queue {
            MessageUtils.sendMsg(MessageConfig.Builder()
                .setChannel(channel)
                .setMessage("${it.size}/50 messages pinned in this channel")
                .build())
        }
    }

    @JvmStatic
    fun restoreCustomCommand(commandId: Int, variables: Variables): String {
        val bool = variables.apis.restoreCustomCommand(commandId, variables)

        if (bool) {
            return "Command Restored"
        }

        return "Could not restore command"
    }
}
