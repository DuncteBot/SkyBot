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

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.skybot.Variables
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import me.duncte123.skybot.objects.user.UnknownUser
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import kotlin.jvm.optionals.getOrNull

class ForceSkip : MusicCommand() {
    init {
        this.name = "forceskip"
        this.aliases = arrayOf("modskip")
        this.help = "Force skips the current track"
        this.usage = "[skip count]"
        this.userPermissions = arrayOf(
            Permission.MANAGE_SERVER
        )
    }

    override fun run(ctx: CommandContext) {
        val mng = ctx.audioUtils.getMusicManager(ctx.guildId)
        val player = mng.player.getOrNull()
        val currTrack = player?.track

        if (currTrack == null) {
            sendMsg(ctx, "The player is not playing.")
            return
        }

        val args = ctx.args
        val scheduler = mng.scheduler

        val count = if (args.isNotEmpty()) {
            if (!args[0].matches("\\d{1,10}".toRegex())) {
                1
            } else {
                args[0].toInt().coerceIn(1, scheduler.queue.size.coerceAtLeast(1))
            }
        } else {
            1
        }

        val trackData = scheduler.getUserData(currTrack)

        scheduler.skipTracks(count, false)

        // Return the console user if the requester is null
        val user = ctx.jda.getUserById(trackData.requester) ?: UnknownUser()

        val newTrack = player.track

        if (newTrack == null) {
            sendMsg(
                ctx,
                "Successfully skipped $count tracks.\n" +
                    "Queue is now empty."
            )
            return
        }

        sendEmbed(
            ctx,
            EmbedUtils.embedMessage(
                "Successfully skipped $count tracks.\n" +
                    "Now playing: ${newTrack.info.title}\n" +
                    "Requester: ${user.asTag}"
            )
                .setThumbnail(newTrack.info.artworkUrl)
        )
    }

    override fun getSubData(): SubcommandData {
        return super.getSubData()
            .addOption(
                OptionType.INTEGER,
                "skip_count",
                "The number of tracks to skip",
                false
            )
    }

    override fun handleEvent(
        event: SlashCommandInteractionEvent,
        guild: DunctebotGuild,
        variables: Variables
    ) {
        //
    }
}
