package ml.duncte123.skybot.commands.uncategorized

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class SuggestCommand: Command() {

    val suggestMessage = AirUtils.CONFIG.get("messages.suggest")
            ?: "Put your suggestions at [our Trello board](https://trello.com/b/iSaxpcGR/skybot-suggestions \"Suggest to Dunctebot here!\""

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        var embed = EmbedUtils.defaultEmbed()
                .build()

        event.author.openPrivateChannel().queue({
            it.sendMessage(embed).queue({}, {
                event.channel.sendMessage(embed).queue()
            })
        })
    }

    override fun getName() = "suggest"

    override fun help() = "Suggest something to be in Dunctebot!"
}
