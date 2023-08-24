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

package ml.duncte123.skybot.commands.music

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import kotlin.math.max
import kotlin.math.min

class VolumeCommand : MusicCommand() {

    init {
        this.name = "volume"
        this.help = "Sets the volume on the music player"
        this.usage = "[volume]"
    }

    override fun run(ctx: CommandContext) {
        if (!isUserOrGuildPatron(ctx)) {
            return
        }

        val mng = ctx.audioUtils.getMusicManager(ctx.guild)
        val player = mng.player
        val args = ctx.args

        if (args.isEmpty()) {
            sendMsg(ctx, "The current volume is **${player.volume}%**")
            return
        }

        try {
            val userInput = args[0].toInt()
            val newVolume = max(0, min(1000, userInput))
            val oldVolume = player.volume

            player.volume = newVolume

            sendMsg(ctx, "Player volume changed from **$oldVolume%** to **$newVolume%**")
        } catch (e: NumberFormatException) {
            sendMsg(ctx, "**${args[0]}** is not a valid integer. (0 - 1000)")
        }
    }

    override fun getSubData(): SubcommandData {
        return super.getSubData()
            .addOptions(
                OptionData(
                    OptionType.INTEGER,
                    "volume",
                    "Sets the volume on the music player (0-1000 where 100 is 100%)",
                    false
                )
                    .setMinValue(0)
                    .setMinValue(1000)
            )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        if (!isUserOrGuildPatron(event)) {
            return
        }

        val mng = variables.audioUtils.getMusicManager(event.guild!!.idLong)
        val player = mng.player
        val volumeOpt = event.getOption("volume")

        if (volumeOpt == null) {
            event.reply("The current volume is **${player.volume}%**").queue()
            return
        }

        try {
            val userInput = volumeOpt.asInt
            val newVolume = max(0, min(1000, userInput))
            val oldVolume = player.volume

            player.volume = newVolume

            event.reply("Player volume changed from **$oldVolume%** to **$newVolume%**").queue()
        } catch (e: NumberFormatException) {
            event.reply("**${volumeOpt.asString}** is not a valid integer. (0 - 1000)").queue()
        }
    }
}
