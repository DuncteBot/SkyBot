package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class RestartCommand : MusicCommand() {

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (!channelChecks(event))
            return

        val player = getMusicManager(event.guild).player

        if (player.playingTrack == null) {
            MessageUtils.sendError(event.message)
            MessageUtils.sendMsg(event, "No track currently playing")
        }

        player.seekTo(0)
        MessageUtils.sendSuccess(event.message)
    }

    override fun help() = "Start the current track back to the beginning"

    override fun getName() = "restart"
}