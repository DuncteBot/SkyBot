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

import com.sun.management.OperatingSystemMXBean
import fredboat.audio.player.LavalinkManager
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.skybot.objects.command.Command
import me.duncte123.skybot.objects.command.CommandCategory
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.utils.AirUtils
import java.io.File
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import kotlin.math.floor

class StatsCommand : Command() {
    init {
        this.category = CommandCategory.UTILS
        this.name = "stats"
        this.help = "Shows statistics about the bot"
    }

    override fun execute(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            sendNormalStats(ctx)
            return
        }

        when (ctx.args[0]) {
            "lavalink", "ll" -> sendLavalinkStats(ctx)
        }
    }

    private fun sendNormalStats(ctx: CommandContext) {
        val shardManager = ctx.shardManager
        val connectedVC = shardManager.shardCache.map { shard ->
            shard.voiceChannelCache.filter { vc ->
                vc.members.contains(vc.guild.selfMember)
            }.count()
        }.sum()

        val uptimeLong = ManagementFactory.getRuntimeMXBean().uptime
        val serverUptimeString = AirUtils.getUptime(getSystemUptimeSeconds() * 1000)

        val platformMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        val cores = platformMXBean.availableProcessors
        val serverCpuUsage = DecimalFormat("###.###%").format(platformMXBean.cpuLoad)
        val serverMem = (platformMXBean.totalMemorySize shr 20).toDouble()
        val serverMemUsage = serverMem - (platformMXBean.freeMemorySize shr 20)
        val serverMemPercent = floor((serverMemUsage / serverMem) * 100.0)

        val memoryBean = ManagementFactory.getMemoryMXBean()
        val jvmCpuUsage = DecimalFormat("###.###%").format(platformMXBean.processCpuLoad)
        val jvmMemUsage = (memoryBean.heapMemoryUsage.used shr 20).toDouble()
        val jvmMemTotal = (memoryBean.heapMemoryUsage.max shr 20).toDouble()
        val jvmMemPercent = floor((jvmMemUsage / jvmMemTotal) * 100)
        val os = "${platformMXBean.name} ${platformMXBean.arch} ${platformMXBean.version}"

        val threadBean = ManagementFactory.getThreadMXBean()

        val embed = EmbedUtils.getDefaultEmbed()
            .addField(
                "Discord/bot Stats",
                """**Guilds cache:** ${shardManager.guildCache.size()}
                    |**User cache:** ${shardManager.userCache.size()}
                    |**TextChannel cache:** ${shardManager.textChannelCache.size()}
                    |**VoiceChannel cache:** ${shardManager.voiceChannelCache.size()}
                    |**GuildSetting cache:** ${ctx.variables.guildSettingsCache.size}
                    |**Playing music count:** $connectedVC
                    |**Uptime:** ${AirUtils.getUptime(uptimeLong)}
                """.trimMargin(),
                false
            )
            .addField(
                "Server stats",
                """**CPU cores:** $cores
                    |**CPU usage:** $serverCpuUsage
                    |**Ram:** ${serverMemUsage}MB / ${serverMem}MB ($serverMemPercent%)
                    |**System uptime:** $serverUptimeString
                    |**Operating System:** $os
                """.trimMargin(),
                false
            )
            .addField(
                "JVM stats",
                """**CPU usage:** $jvmCpuUsage
                    |**Threads:** ${threadBean.threadCount} / ${threadBean.peakThreadCount}
                            |**Ram:** ${jvmMemUsage}MB / ${jvmMemTotal}MB ($jvmMemPercent%)
                """.trimMargin(),
                false
            )

        sendEmbed(ctx, embed)
    }

    private fun sendLavalinkStats(ctx: CommandContext) {
        val llm = LavalinkManager.INS

        if (!llm.isEnabled) {
            sendMsg(ctx, "Not enabled")
            return
        }

        val availableNodes = llm.lavalink.nodes.filter { it.available }

        val embed = EmbedUtils.getDefaultEmbed()
            .setFooter("Available nodes: ${availableNodes.size}")

        availableNodes.forEachIndexed { index, node ->
            val stats = node.stats ?: return@forEachIndexed

            embed.addField(
                "Lavalink node #$index",
                """**Uptime:** ${AirUtils.getUptime(stats.uptime)}
                    |**CPU cores:** ${stats.cpu.cores}
                    |**System Load:** ${stats.cpu.systemLoad}%
                    |**Used memory:** ${stats.memory.used shr 20}MB
                    |**Free memory:** ${stats.memory.free shr 20}MB
                    |**Players:** ${stats.players}
                    |**Players playing:** ${stats.playingPlayers}
                """.trimMargin(),
                true
            )
        }

        sendEmbed(ctx, embed)
    }

    // This code has been inspired from the oshi uptime method: https://duncte.bot/oGfi
    // I decided to implement the part I needed here to not have to pull in the entire oshi-core lib
    // Why? This code is expected to be run in linux envs in docker so I don't care about windows or mac
    private fun getSystemUptimeSeconds(): Long {
        val procFile = File("/proc/uptime")

        // not on linux?
        if (!procFile.exists()) {
            return 0
        }

        val uptime = procFile.readText()
        val spaceIndex = uptime.indexOf(' ')

        if (spaceIndex < 0) {
            return 0
        }

        return try {
            // convert to long because that is what oshi returned to us in the past
            uptime.substring(0, spaceIndex).toDouble().toLong()
        } catch (ignored: NumberFormatException) {
            0
        }
    }
}
