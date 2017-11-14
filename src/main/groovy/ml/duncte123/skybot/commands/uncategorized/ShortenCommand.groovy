package ml.duncte123.skybot.commands.uncategorized

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.Settings
import ml.duncte123.skybot.utils.WebUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class ShortenCommand extends Command {

    ShortenCommand() {
        this.category = CommandCategory.UNLISTED
    }

    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if(args.length < 1 || args[0].isEmpty()) {
            sendMsg(event, "Incorrect usage: `${Settings.prefix}$name <link to shorten>`")
            return
        }
        String shortenedUrl = WebUtils.shortenUrl(args[0])
        sendMsg(event, "Here is your shortened url: <$shortenedUrl>")
    }

    @Override
    String help() {
        return "Shortens a url\n" +
                "Usage: `${Settings.prefix}$name <link to shorten>`"
    }

    @Override
    String getName() {
        return "shorten"
    }

    @Override
    String[] getAliases() {
        return ["short", "url", "bitly"]
    }
}
