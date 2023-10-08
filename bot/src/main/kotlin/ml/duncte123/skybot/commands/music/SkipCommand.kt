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
import ml.duncte123.skybot.Settings.NO_STATIC
import ml.duncte123.skybot.Settings.YES_STATIC
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.objects.TrackUserData
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import kotlin.math.ceil

class SkipCommand : MusicCommand() {

    init {
        this.name = "skip"
        this.aliases = arrayOf("next", "nexttrack", "skiptrack")
        this.help = "Skips the current track"
    }

    override fun run(ctx: CommandContext) {
        skipHandler(ctx.variables, ctx.guild, ctx.author) { sendMsg(ctx, it) }
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        event.deferReply().queue()

        skipHandler(variables, event.guild!!, event.user) { event.hook.sendMessage(it).queue() }
    }

    private fun skipHandler(variables: Variables, guild: Guild, user: User, sendMessage: (String) -> Unit) {
        val mng = variables.audioUtils.getMusicManager(guild.idLong)
        val player = mng.player

        if (player.playingTrack == null) {
            sendMessage("The player is not playing.")
            return
        }

        if (!player.playingTrack.isSeekable) {
            sendMessage("This track is not seekable")
            return
        }

        val trackData = player.playingTrack.getUserData(TrackUserData::class.java)

        if (trackData.requester == user.idLong) {
            doSkip(mng, sendMessage)
            return
        }
		
        val vc = getLavalinkManager().getConnectedChannel(guild)
		
        if (vc == null) {
            sendMessage("Somehow I am not connected to a voice channel? Probably a bug, please report this!")
            return
        }

        // https://github.com/jagrosh/MusicBot/blob/master/src/main/java/com/jagrosh/jmusicbot/commands/music/SkipCmd.java
        val listeners = vc.members.filter {
            !it.user.isBot && !(it.voiceState?.isDeafened ?: false)
        }.count()

        val votes = trackData.votes

        var msg = if (votes.contains(user.idLong)) {
            "$NO_STATIC You already voted to skip this song `["
        } else {
            votes.add(user.idLong)
            "$YES_STATIC Successfully voted to skip the song `["
        }

        val skippers = vc.members.count { votes.contains(it.idLong) }
        val required = ceil(listeners * .55).toInt()

        msg += "$skippers votes, $required/$listeners needed]`"

        sendMessage(msg)

        if (skippers >= required) {
            doSkip(mng, sendMessage)
        }
    }

    private fun doSkip(mng: GuildMusicManager, sendMessage: (String) -> Unit) {
        val player = mng.player

        mng.scheduler.specialSkipCase()

        if (player.playingTrack == null) {
            sendMessage(
                "Successfully skipped the track.\n" +
                    "Queue is now empty."
            )
        }
    }
}
