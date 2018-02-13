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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.unstable.commands.essentials

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.TFException
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.unstable.utils.ComparatingUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class TraceDataCommand : Command() {

    init {
        this.category = CommandCategory.UNLISTED
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        @Suppress("DEPRECATION")
        if (!Settings.wbkxwkZPaG4ni5lm8laY.contains(event.author.id))
            return

        val args = when (invoke) {
            "exact" -> arrayOf("exact")
            "atomic" -> if (args.size == 1) arrayOf("atomic", args[0]) else return
            else -> { args }
        }

        when {
            args.isEmpty() -> ComparatingUtils.provideData(event.channel)
            args.size == 1 && args[0] == "exact" -> ComparatingUtils.provideExactData(event.channel)
            args.size == 1 && args[0] == "test" -> throw TFException("lol")
            args.size == 2 && args[0] == "atomic" -> ComparatingUtils.provideAtomicData(event.channel, args[1])
        }
    }

    override fun help(): String = """Hidden Command for the devs only""".trimMargin()

    override fun getName(): String = "tracedata"

    override fun getAliases(): Array<String> = arrayOf("traces", "debugdata", "stacks", "exact", "atomic")
}