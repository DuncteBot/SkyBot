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

package ml.duncte123.skybot.commands.music

import lavalink.client.player.LavalinkPlayer
import lavalink.client.player.LavaplayerPlayerWrapper
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

class BaseBoostCommand : MusicCommand() {
    override fun run(ctx: CommandContext) {
        if (!isUserOrGuildPatron(ctx.event)) {
            return
        }

        if (!getLavalinkManager().isEnabled) {
            sendMsg(ctx, "Lavalink is required for this")

            return
        }

        val args = ctx.args

        if (args.isEmpty()) {
            sendMsg(ctx, "Missing arguments: `${ctx.prefix}${ctx.invoke} <high/med/low/off>`")

            return
        }

        val gain = when(args[0]) {
            "high" -> 0.25
            "med" -> 0.15
            "low" -> 0.05
            "off", "none" -> 0.0

            else -> {
                sendMsg(ctx, "Unknown bassboost preset ${args[0]}, please choose from high/med/low/off")
                -1.0
            }
        }

        if (gain < 0) {
            return
        }

        sendMsg(ctx, "Set the bassboost to `${args[0]}`")
        setLavalinkEQ(gain, ctx)
    }

    override fun getName() = "baseboost"

    override fun getAliases() = arrayOf("bb", "bassboost")

    override fun help(prefix: String) = """Sets the bassboost on the player
        |Usage: `${prefix}bassboost <high/med/low/off>`
    """.trimMargin()

    private fun setLavalinkEQ(gain: Double, ctx: CommandContext) {
        val node = (getMusicManager(ctx.guild, ctx.audioUtils).player as LavalinkPlayer).link.getNode(false) ?: return
        val jackson = ctx.variables.jackson

        val json = jackson.createObjectNode()
        val array = json.putArray("bands")
        json.put("op", "equalizer")
        json.put("guildId", ctx.guild.id)

        for (i in 0..2) {
            val band = jackson.createObjectNode()
                .put("band", i)
                .put("gain", gain)
            array.add(band)
        }

        node.send(jackson.writeValueAsString(json))
    }
}
