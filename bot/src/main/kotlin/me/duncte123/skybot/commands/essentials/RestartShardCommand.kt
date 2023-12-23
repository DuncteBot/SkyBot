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

package me.duncte123.skybot.commands.essentials

import io.sentry.Sentry
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.skybot.EventManager
import me.duncte123.skybot.objects.command.Command
import me.duncte123.skybot.objects.command.CommandCategory
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.utils.AirUtils
import me.duncte123.skybot.utils.AudioUtils
import me.duncte123.skybot.utils.CommandUtils.isDev
import net.dv8tion.jda.api.sharding.ShardManager
import java.lang.Thread.sleep
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RestartShardCommand : Command() {
    private val thread = Executors.newVirtualThreadPerTaskExecutor()
    private val restartInSec = 5L

    init {
        this.category = CommandCategory.UNLISTED
        this.name = "restartshard"
        this.aliases = arrayOf("shardrestart")
        this.help = "Restart the bot or a shard"
        this.usage = "[shard id]"
    }

    override fun execute(ctx: CommandContext) {
        if (!isDev(ctx.author)) {
            return
        }

        val shardManager = ctx.jda.shardManager!!

        try {
            when (ctx.args.size) {
                0 -> {
                    MessageUtils.sendMsg(ctx, "All shards will restart in $restartInSec seconds")
                    EventManager.shouldFakeBlock = true
                    EventManager.restartingShard = -1
                    terminate(-1, shardManager, ctx.audioUtils)
                    thread.execute {
                        sleep(TimeUnit.SECONDS.toMillis(restartInSec))
                        shardManager.restart()

                        EventManager.restartingShard = -32
                        EventManager.shouldFakeBlock = false
                    }
                }
                1 -> {
                    val id = ctx.args[0].toInt()

                    if (id > shardManager.shardsTotal - 1) {
                        MessageUtils.sendMsg(ctx, "$id is an invalid shard id")
                        return
                    }

                    MessageUtils.sendMsg(ctx, "Shard $id will restart in $restartInSec seconds")
                    EventManager.shouldFakeBlock = true
                    EventManager.restartingShard = id
                    terminate(id, shardManager, ctx.audioUtils)
                    thread.execute {
                        sleep(TimeUnit.SECONDS.toMillis(restartInSec))
                        shardManager.restart(id)

                        EventManager.restartingShard = -32
                        EventManager.shouldFakeBlock = false
                    }
                }
                else -> MessageUtils.sendError(ctx.message)
            }
        } catch (ex: NumberFormatException) {
            MessageUtils.sendError(ctx.message)
            Sentry.captureException(ex)
        }
    }

    private fun terminate(shard: Int, shardManager: ShardManager, audioUtils: AudioUtils) {
        for (jda in shardManager.shardCache) {
            if (jda.shardInfo.shardId != shard && shard != -1) {
                continue
            }

            for (guild in jda.guildCache) {
                AirUtils.stopMusic(guild.idLong, audioUtils)
            }
        }
    }
}
