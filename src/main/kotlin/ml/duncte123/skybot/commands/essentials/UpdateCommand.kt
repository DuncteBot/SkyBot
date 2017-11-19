package ml.duncte123.skybot.commands.essentials

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.*

class UpdateCommand: Command() {

    init {
        this.category = CommandCategory.UNLISTED
    }
    
    override fun executeCommand(invoke: String, args: Array<out String>?, event: GuildMessageReceivedEvent) {
        if (!Arrays.asList<String>(*Settings.wbkxwkZPaG4ni5lm8laY).contains(event.author.id)
                && Settings.ownerId != event.author.id) {
            event.channel.sendMessage(":x: ***YOU ARE DEFINITELY THE OWNER OF THIS BOT***").queue()
            sendError(event.message)
            return
        }
        
        event.channel.sendMessage(":heavy_check_mark: Goodbye").queue()
        
        // This will also shutdown eval
        event.jda.asBot().shardManager.shutdown()
        
        // Stop everything that my be using resources
        AirUtils.stop()
        
        // Magic code. Tell the updater to update
        System.exit(0x5454)
    }

    override fun help()= "Update the bot and restart"
    
    override fun getName()= "update"
}