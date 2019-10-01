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

package ml.duncte123.skybot.commands.essentials

import com.sun.management.OperatingSystemMXBean
import me.duncte123.botcommons.messaging.EmbedUtils.defaultEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import oshi.SystemInfo
import java.lang.management.ManagementFactory
import java.sql.Time
import java.text.DecimalFormat
import java.util.function.BiFunction
import kotlin.math.floor

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class StatsCommand : Command() {
    private val oshi = SystemInfo().hardware.processor

    init {
        this.category = CommandCategory.UTILS
        this.name = "stats"
        this.helpFunction = BiFunction {_, _ -> "Shows some nerdy statistics about the bot" }
    }

    override fun execute(ctx: CommandContext) {

        val shardManager = ctx.shardManager
        val connectedVC = shardManager.shardCache.map { shard ->
            shard.voiceChannelCache.filter { vc ->
                vc.members.contains(vc.guild.selfMember)
            }.count()
        }.sum()

        val uptimeLong = ManagementFactory.getRuntimeMXBean().uptime
        val uptimeTime = Time(uptimeLong - 3600000)
        val serverUptimeString = AirUtils.getUptime(oshi.systemUptime * 1000, true)
        val cores = ManagementFactory.getOperatingSystemMXBean().availableProcessors
        val platformMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        val serverCpuUsage = DecimalFormat("###.###%").format(platformMXBean.systemCpuLoad)
        val serverMem = (platformMXBean.totalPhysicalMemorySize shr 20).toDouble()
        val serverMemUsage = serverMem - (platformMXBean.freePhysicalMemorySize shr 20)
        val serverMemPercent = floor((serverMemUsage / serverMem) * 100.0)

        val jvmCpuUsage = DecimalFormat("###.###%").format(platformMXBean.processCpuLoad)
        val jvmMemUsage = (ManagementFactory.getMemoryMXBean().heapMemoryUsage.used shr 20).toDouble()
        val jvmMemTotal = (ManagementFactory.getMemoryMXBean().heapMemoryUsage.max shr 20).toDouble()
        val jvmMemPercent = floor((jvmMemUsage / jvmMemTotal) * 100)
        val os = "${platformMXBean.name} ${platformMXBean.arch} ${platformMXBean.version}"

        val embed = defaultEmbed()

            .addField("Discord/bot Stats",
                """**Guilds:** ${shardManager.guildCache.size()}
                    |**Users (unique):** ${shardManager.userCache.size()}
                    |**Text channels:** ${shardManager.textChannelCache.size()}
                    |**Voice channels:** ${shardManager.voiceChannelCache.size()}
                    |**Playing music count:** $connectedVC
                    |**Uptime:** ${AirUtils.getUptime(uptimeLong)} $uptimeTime
                """.trimMargin(), false)

            .addField("Server stats",
                """**CPU cores:** $cores
                    |**CPU usage:** $serverCpuUsage
                    |**Ram:** ${serverMemUsage}MB / ${serverMem}MB ($serverMemPercent%)
                    |**System uptime:** $serverUptimeString
                    |**Operating System:** $os
                """.trimMargin(), false)

            .addField("JVM stats",
                """**CPU usage:** $jvmCpuUsage
                    |**Threads:** ${Thread.activeCount()} / ${Thread.getAllStackTraces().keys.size}
                            |**Ram:** ${jvmMemUsage}MB / ${jvmMemTotal}MB ($jvmMemPercent%)
                        """.trimMargin(), false)

        sendEmbed(ctx.event, embed)

    }
}
