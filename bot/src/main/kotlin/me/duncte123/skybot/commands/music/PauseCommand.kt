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

package me.duncte123.skybot.commands.music

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.skybot.Variables
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class PauseCommand : MusicCommand() {
    init {
        this.name = "pause"
        this.aliases = arrayOf("resume")
        this.help = "Pauses the current song"
    }

    override fun run(ctx: CommandContext) {
        val mng = ctx.audioUtils.getMusicManager(ctx.guildId)
        val localPlayer = mng.player

        localPlayer.ifPresentOrElse({ player ->
            if (player.track == null) {
                sendMsg(ctx, "Cannot pause or resume player because no track is loaded for playing.")
                return@ifPresentOrElse
            }

            player.setPaused(!player.paused).subscribe {
                sendMsg(ctx, "The player has ${if (it.paused) "been paused" else "resumed playing"}.")
            }
        }) {
            sendMsg(ctx, "Cannot pause or resume player because no track is loaded for playing.")
        }
    }

    override fun handleEvent(
        event: SlashCommandInteractionEvent,
        guild: DunctebotGuild,
        variables: Variables,
    ) {
        val mng = variables.audioUtils.getMusicManager(event.guild!!.idLong)
        val localPlayer = mng.player

        localPlayer.ifPresentOrElse({ player ->
            if (player.track == null) {
                event.reply("Cannot pause or resume player because no track is loaded for playing.").queue()
                return@ifPresentOrElse
            }

            player.setPaused(!player.paused).subscribe {
                event.reply("The player has ${if (it.paused) "been paused" else "resumed playing"}.").queue()
            }
        }) {
            event.reply("Cannot pause or resume player because no track is loaded for playing.").queue()
        }
    }
}
