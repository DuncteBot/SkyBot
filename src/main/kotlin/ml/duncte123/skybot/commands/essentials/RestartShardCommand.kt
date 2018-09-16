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
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import me.duncte123.botCommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.EventManager
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.bot.sharding.ShardManager
import java.util.concurrent.TimeUnit

@SinceSkybot("3.50.X")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
class RestartShardCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
    }

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (!isDev(event.author)) return
        val shardManager = event.jda.asBot().shardManager

        try {
            when (ctx.args.size) {
                0 -> {
                    MessageUtils.sendMsg(ctx.event, "All shards will restart in 15 seconds")
                    EventManager.shouldFakeBlock = true
                    EventManager.restartingShard = -1
                    terminate(-1, event.jda.asBot().shardManager)
                    launch {
                        delay(15, TimeUnit.SECONDS)
                        shardManager.restart()

                        EventManager.restartingShard = -32
                        EventManager.shouldFakeBlock = false
                    }
                }
                1 -> {
                    val id = ctx.args[0].toInt()

                    if(id > ctx.shardManager.shardsTotal) {
                        MessageUtils.sendMsg(ctx.event, "$id is an invalid shard id")
                        return
                    }

                    MessageUtils.sendMsg(ctx.event, "Shard $id will restart in 15 seconds")
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
                ml.duncte123.skybot.utils.MessageUtils.sendErrorJSON(event.message, ex, false)
            else {
                MessageUtils.sendError(event.message)
            }
        }
    }

    override fun help() = "Restart the bot or a shard\nUsage: $PREFIX$name [shard id]`"

    override fun getName() = "restartshard"

    override fun getAliases() = arrayOf("shardrestart")

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
