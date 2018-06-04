package ml.duncte123.skybot.commands.uncategorized

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class SuggestCommand : Command() {

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        MessageUtils.sendMsg(event, "You can leave suggestions for the bot on his page: https://bot.duncte123.me/suggest.html")
    }

    override fun getName() = "suggest"

    override fun help() = "Suggest something to be in Dunctebot!"
}
