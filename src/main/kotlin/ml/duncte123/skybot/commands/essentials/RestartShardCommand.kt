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

package ml.duncte123.skybot.commands.essentials

import kotlinx.coroutines.*
import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.EventManager
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.CommandUtils.isDev
import ml.duncte123.skybot.utils.JSONMessageErrorsHelper
import net.dv8tion.jda.api.sharding.ShardManager
import java.util.concurrent.TimeUnit

@SinceSkybot("3.50.X")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
class RestartShardCommand : Command() {

    private val restartInSec = 5L

    init {
        this.category = CommandCategory.UNLISTED
        this.name = "restartshard"
        this.aliases = arrayOf("shardrestart")
        this.help = "Restart the bot or a shard"
        this.usage = "[shard id]"
    }

    override fun execute(ctx: CommandContext) {

        val event = ctx.event

        if (!isDev(event.author)) {
            return
        }

        val shardManager = event.jda.shardManager!!

        try {
            when (ctx.args.size) {
                0 -> {
                    MessageUtils.sendMsg(ctx, "All shards will restart in $restartInSec seconds")
                    EventManager.shouldFakeBlock = true
                    EventManager.restartingShard = -1
                    terminate(-1, shardManager, ctx.audioUtils)
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                        delay(TimeUnit.SECONDS.toMillis(restartInSec))
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
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                        delay(TimeUnit.SECONDS.toMillis(restartInSec))
                        shardManager.restart(id)

                        EventManager.restartingShard = -32
                        EventManager.shouldFakeBlock = false
                    }
                }
                else -> MessageUtils.sendError(event.message)
            }
        } catch (ex: NumberFormatException) {
            if (Settings.USE_JSON) {
                JSONMessageErrorsHelper.sendErrorJSON(event.message, ex, false, ctx.variables.jackson)
            } else {
                MessageUtils.sendError(event.message)
            }
        }
    }

    private fun terminate(shard: Int, shardManager: ShardManager, audioUtils: AudioUtils) {
        for (jda in shardManager.shardCache) {
            if (jda.shardInfo.shardId != shard && shard != -1) {
                continue
            }

            for (guild in jda.guildCache) {
                AirUtils.stopMusic(guild, audioUtils)
            }
        }
    }
}
