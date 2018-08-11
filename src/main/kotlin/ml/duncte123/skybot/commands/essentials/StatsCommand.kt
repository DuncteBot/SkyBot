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

import com.sun.management.OperatingSystemMXBean
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils.defaultEmbed
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import java.lang.management.ManagementFactory
import java.sql.Time
import java.text.DecimalFormat

class StatsCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {

        val shardManager = ctx.shardManager
        val connectedVC = shardManager.shards.map {
            shard -> shard.voiceChannelCache.filter {
                vc -> vc.members.contains(vc.guild.selfMember)
            }.count()
        }.sum()
        val uptimeLong = ManagementFactory.getRuntimeMXBean().uptime
        val uptimeTime = Time(uptimeLong - 3600000)
        val serverUptimeLong = AirUtils.getSystemUptime()
        val serverUptimeTime = Time(serverUptimeLong - 3600000)
        val cores = ManagementFactory.getOperatingSystemMXBean().availableProcessors
        val platformMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        val processUsage = DecimalFormat("###.###%").format(platformMXBean.processCpuLoad)
        val serverMem = platformMXBean.totalPhysicalMemorySize
        val serverUsage = serverMem - platformMXBean.freePhysicalMemorySize
        val jvmMem = ManagementFactory.getMemoryMXBean().heapMemoryUsage.used shr 20
        val jvmUsage = ManagementFactory.getMemoryMXBean().heapMemoryUsage.max shr 20
        val OS = "${platformMXBean.name} ${platformMXBean.arch} ${platformMXBean.version}"

        val embed = defaultEmbed()

                .addField("Discord/bot Stats",
                  """**Guilds:**
                    |**Users (unique): ${shardManager.userCache.size()}**
                    |**Text channels: ${shardManager.textChannelCache.size()}**
                    |**Voice channels: ${shardManager.voiceChannelCache.size()}**
                    |~~**Emotes: @Deprecated("EmoteCache disabled") **~~
                    |**Playing music count: $connectedVC**
                    |**Uptime: ${AirUtils.getUptime(uptimeLong)} $uptimeTime**
                """.trimMargin(), false)

                .addField("Server stats",
                  """**CPU's: $cores**
                    |**CPU usage: $processUsage**
                    |**Total ram: ${serverMem shr 20}**
                    |**Ram usage: ${serverUsage shr 20}**
                    |**System uptime: ${AirUtils.getUptime(serverUptimeLong)} $serverUptimeTime**
                    |**Operating System: $OS**
                """.trimMargin(), false)

                .addField("JVM stats",
                          """**Total thread count: ${Thread.getAllStackTraces().keys.size}**
                            |**Active thread count: ${Thread.activeCount()}**
                            |**Used ram: $jvmMem**
                            |**Allocated ram: $jvmUsage**
                        """.trimMargin(), false)

        sendEmbed(ctx.event, embed.build())

    }

    override fun getName() = "stats"

    override fun help() = "Shows some nerdy stats about the bot"

    override fun getCategory() = CommandCategory.NERD_STUFF
}