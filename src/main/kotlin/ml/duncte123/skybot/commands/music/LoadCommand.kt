package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONTokener
import java.util.*

class LoadCommand : MusicCommand() {

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (!channelChecks(event))
            return

        var attachments = event.message.attachments

        if (attachments.size == 0) {
            MessageUtils.sendError(event.message)
            MessageUtils.sendMsg(event, "No attachment given")
            return
        }

        if (attachments.size > 1) {
            MessageUtils.sendError(event.message)
            MessageUtils.sendMsg(event, "Please only attach one file at a time")
            return
        }

        var attachment = attachments[0]

        attachment.withInputStream({
            try {
                // We have to do it this way because
                // JSONArray doesn't accept a raw InputStream
                var array = JSONArray(JSONTokener(it))

                array.filter(Objects::nonNull)
                        .forEach {
                            // This probably announces it to the channel
                            AudioUtils.ins.loadAndPlay(getMusicManager(event.guild),
                                    event.channel,
                                    it.toString(),
                                    false,
                                    false)
                        }

                MessageUtils.sendEmbed(event, EmbedUtils.embedField(AudioUtils.ins.embedTitle,
                        "Added ${array.length()} requested tracks."))
            } catch (exception: JSONException) {
                MessageUtils.sendError(event.message)
                MessageUtils.sendMsg(event, "Invalid JSON file!")
            }
        })
    }

    override fun getName() = "load"

    override fun help() = "Loads a given playlist file"
}