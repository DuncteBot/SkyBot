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

class BassBoostCommand : MusicCommand() {

    init {
        this.name = "bassboost"
        this.aliases = arrayOf("bb", "baseboost")
        this.help = "Sets the bass boost on the music player"
        this.usage = "<high/med/low/off>"
    }

    override fun run(ctx: CommandContext) {
        if (!isUserOrGuildPatron(ctx)) {
            return
        }

        if (!getLavalinkManager().isEnabled) {
            sendMsg(ctx, "Lavalink is required for this")

            return
        }

        val args = ctx.args

        if (args.isEmpty()) {
            this.sendUsageInstructions(ctx)

            return
        }

        val gain = when (args[0]) {
            "high" -> 0.25f
            "med" -> 0.15f
            "low" -> 0.05f
            "off", "none" -> 0.0f

            else -> {
                sendMsg(ctx, "Unknown bassboost preset ${args[0]}, please choose from high/med/low/off")
                -1.0f
            }
        }

        if (gain < 0) {
            return
        }

        sendMsg(ctx, "Set the bassboost to `${args[0]}`")
        setLavalinkEQ(gain, ctx.variables, ctx.guildId)
    }

    private fun setLavalinkEQ(gain: Float, variable: Variables, guildId: Long) {
        val player = variable.audioUtils.getMusicManager(guildId).player
        val filters = player.filters

        for (i in 0..2) {
            filters.setBand(i, gain)
        }

        filters.commit()
    }

    override fun getSubData(): SubcommandData {
        return super.getSubData()
            .addOptions(
                OptionData(
                    OptionType.STRING,
                    "level",
                    "How strong should the bass boost be",
                    true,
                    false,
                )
                    .addChoice("high", "high")
                    .addChoice("med", "med")
                    .addChoice("low", "low")
                    .addChoice("off", "off")
            )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        if (!isUserOrGuildPatron(event)) {
            return
        }

        if (!getLavalinkManager().isEnabled) {
            event.reply("Lavalink is required for this").queue()

            return
        }

        val arg = event.getOption("level")!!.asString

        val gain = when (arg) {
            "high" -> 0.25f
            "med" -> 0.15f
            "low" -> 0.05f
            "off" -> 0.0f

            else -> {
                event.reply(
                    "(this should never happen) Unknown bassboost preset ${arg}, please choose from high/med/low/off"
                ).queue()
                -1.0f
            }
        }

        if (gain < 0) {
            return
        }

        event.reply("Set the bassboost to `${arg}`").queue()
        setLavalinkEQ(gain, variables, event.guild!!.idLong)
    }
}
