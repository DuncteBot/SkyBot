package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.json.JSONArray

import java.nio.charset.StandardCharsets.UTF_8

class SaveCommand : MusicCommand() {

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        event.channel.sendFile(
                toByteArray(event.guild),
                "playlist.json",
                MessageBuilder()
                        .append(event.author)
                        .append(", here is the queue which can be re-imported")
                        .build()).queue()
    }

    private fun toByteArray(guild: Guild?): ByteArray {
        var array = JSONArray()
        var manager = getMusicManager(guild)

        var urls = manager.scheduler.queue
                .map { it.identifier }
                .toMutableList()

        urls.add(0, manager.player.playingTrack.identifier)

        for (x in urls)
            array.put(x)

        return array.toString(2).toByteArray(UTF_8)
    }

    override fun getName() = "save"

    override fun help() = "Saves a playlist into a file with can be loaded with ${Settings.PREFIX}load"
}