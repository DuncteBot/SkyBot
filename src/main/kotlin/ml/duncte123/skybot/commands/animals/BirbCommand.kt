package ml.duncte123.skybot.commands.animals

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.WebUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.io.IOException

class BirbCommand: Command() {

    init {
        this.category = CommandCategory.ANIMALS
    }

    override fun executeCommand(invoke: String?, args: Array<out String>?, event: GuildMessageReceivedEvent?) {
        try {
            sendEmbed(event, EmbedUtils.embedImage("https://proximyst.com:4500/image/" +
                            "${WebUtils.getText("https://proximyst.com:4500/random/path/text")}/image"))
        } catch (e: IOException) {
            sendMsg(event, "ERROR: " + e.message)
        }
    }

    override fun getName() = "birb"

    override fun help() = "Here is a birb"

    override fun getAliases()= arrayOf("bird")
}