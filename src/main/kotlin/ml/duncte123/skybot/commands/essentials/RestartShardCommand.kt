/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.commands.essentials

import fredboat.audio.player.LavalinkManager
import kotlinx.coroutines.experimental.cancel
import kotlinx.coroutines.experimental.cancelChildren
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import ml.duncte123.skybot.*
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.unstable.utils.ComparatingUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.TimeUnit

@SinceSkybot("3.50.X")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
class RestartShardCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        @Suppress("DEPRECATION")
        if (!isDev(event.author)) return
        val shardManager = event.jda.asBot().shardManager

        try {
            when (args.size) {
                0 -> {
                    EventManager.shouldFakeBlock = true
                    EventManager.restartingShard = -1
                    terminate(-1, event.jda.asBot().shardManager)
                    launch {
                        delay(15, TimeUnit.SECONDS)
                        shardManager.restart()

                        EventManager.restartingShard = -32
                        EventManager.shouldFakeBlock = false
                        val end = EndReached()
                        this.coroutineContext.cancelChildren(end)
                        this.coroutineContext.cancel(end)
                    }
                }
                1 -> {
                    val id = args[0].toInt()
                    EventManager.shouldFakeBlock = true
                    EventManager.restartingShard = id
                    terminate(id, event.jda.asBot().shardManager)
                    launch {
                        delay(15, TimeUnit.SECONDS)
                        shardManager.restart(id)

                        EventManager.restartingShard = -32
                        EventManager.shouldFakeBlock = false
                    }
                }
                else -> MessageUtils.sendError(event.message)
            }
        } catch (ex: NumberFormatException) {
            if (Settings.useJSON)
                MessageUtils.sendErrorJSON(event.message, ex, false)
            else {
                ComparatingUtils.checkEx(ex)
                MessageUtils.sendError(event.message)
            }
        }
    }

    override fun help() = "Restart the bot or a shard\nUsage: $PREFIX$name [shard id]`"

    override fun getName() = "restartshard"

    fun terminate(shard: Int, shardManager: ShardManager) {
        for (jda in shardManager.shardCache) {
            if (jda.shardInfo.shardId != shard && shard != -1)
                continue
            for (guild in jda.guildCache) {
                if (LavalinkManager.ins.isConnected(guild)) {
                    LavalinkManager.ins.closeConnection(guild)
                }
            }
//          for (link in LavalinkManager.ins.lavalink.links) {
//              if (link.jda.shardInfo.shardId == shard || shard == -1)
//                  link.disconnect(); link.resetPlayer(); link.destroy();
//          }
        }
    }
}
