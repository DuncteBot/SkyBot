package me.duncte123.skybot.commands.essentials;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.AirUtils;
import me.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class WhitelistCommand extends Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        if(args.length < 2) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Usage is `" + Config.prefix + "whitelist <guild id> <guild name>")).queue();
            return false;
        }

        return event.getAuthor().getId().equals(Config.ownerId);
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String guildId = args[0];
        String guildName = StringUtils.join(Arrays.copyOfRange(args, 1, args.length));
        String stat = AirUtils.insetIntoWhitelist(guildId, guildName, event.getJDA().getSelfUser().getId());

        if (!stat.isEmpty())
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + stat)).queue();
        else
            event.getChannel().sendMessage(AirUtils.embedMessage("Successfully added " + guildName + " to the whitelist")).queue();
    }

    @Override
    public String help() {
        return "add a guild to the whitelist";
    }
}
