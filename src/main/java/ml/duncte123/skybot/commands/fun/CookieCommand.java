package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CookieCommand extends Command {

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        sendMsg(event, "<:blobnomcookie_secret:317636549342789632>");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "blobnomcookie";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "cookie";
    }

}
