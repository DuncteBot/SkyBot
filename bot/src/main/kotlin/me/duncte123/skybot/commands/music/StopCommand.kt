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
import me.duncte123.skybot.objects.TrackUserData
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import kotlin.jvm.optionals.getOrNull

class StopCommand : MusicCommand() {
    init {
        this.name = "stop"
        this.help = "Stops the music"
    }

    override fun run(ctx: CommandContext) {
        val mng = ctx.audioUtils.getMusicManager(ctx.guildId)
        val player = mng.player.getOrNull()
        val scheduler = mng.scheduler
        val track = player?.track

        if (track == null) {
            sendMsg(ctx, "The player is not playing.")
            return
        }

        val trackData: TrackUserData = scheduler.getUserData(track)

        if (
            ctx.guild.settings.isAllowAllToStop ||
            trackData.requester == ctx.author.idLong ||
            ctx.member.hasPermission(Permission.MANAGE_SERVER)
        ) {
            mng.scheduler.queue.clear()
            player.setPaused(false).setTrack(null).subscribe()

            sendMsg(ctx, "Playback has been completely stopped and the queue has been cleared.")

            return
        }

        sendMsg(
            ctx,
            "Only the person that started this track " +
                "or people with the `Manage Server` permission can stop this track\n" +
                "(this behaviour can be altered in the dashboard)"
        )
    }

    override fun handleEvent(
        event: SlashCommandInteractionEvent,
        guild: DunctebotGuild,
        variables: Variables,
    ) {
        val mng = variables.audioUtils.getMusicManager(event.guild!!.idLong)
        val player = mng.player.getOrNull()
        val scheduler = mng.scheduler
        val track = player?.track

        if (track == null) {
            event.reply("The player is not playing.").queue()
            return
        }

        val trackData: TrackUserData = scheduler.getUserData(track)
        val dbg = DunctebotGuild(event.guild!!, variables)

        if (
            dbg.settings.isAllowAllToStop ||
            trackData.requester == event.user.idLong ||
            event.member!!.hasPermission(Permission.MANAGE_SERVER)
        ) {
            mng.scheduler.queue.clear()
            player.setPaused(false).setTrack(null).subscribe()

            event.reply("Playback has been completely stopped and the queue has been cleared.").queue()

            return
        }

        event.reply(
            "Only the person that started this track " +
                "or people with the `Manage Server` permission can stop this track\n" +
                "(this behaviour can be altered in the dashboard)"
        ).queue()
    }
}
