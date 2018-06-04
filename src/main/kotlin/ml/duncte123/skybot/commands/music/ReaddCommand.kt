package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class ReaddCommand : MusicCommand() {

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (!channelChecks(event))
            return

        val manager = getMusicManager(event.guild)
        val t = manager.player.playingTrack

        if (t == null) {
            MessageUtils.sendError(event.message)
            MessageUtils.sendMsg(event, "No tracks in queue")
            return
        }

        val track = t.makeClone()

        // This is from AudioUtils.java but in Kotlin
        var title = track.info.title
        if (track.info.isStream) {
            val stream = (AirUtils.COMMAND_MANAGER.getCommand("radio") as RadioCommand)
                    .radioStreams.stream().filter { s -> s.url == track.info.uri }.findFirst()
            if (stream.isPresent)
                title = stream.get().name
        }
        var msg = "Adding to queue: $title"
        if (manager.player.playingTrack == null) {
            msg += "\nand the Player has started playing;"
        }

        manager.scheduler.queue(track)
        MessageUtils.sendSuccess(event.message)
        MessageUtils.sendEmbed(event.channel, EmbedUtils.embedField(AudioUtils.ins.embedTitle, msg))
    }

    override fun help() = "Readd the current track to the end of the queue"

    override fun getName() = "readd"
}