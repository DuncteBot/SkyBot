package ml.duncte123.skybot.commands.essentials;

import ml.duncte123.skybot.commands.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class BlacklistCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {

        if(args.length < 2) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Usage is `" + Config.prefix + "whitelist <guild id> <guild name>`")).queue();
            return false;
        }

        return event.getAuthor().getId().equals(Config.ownerId);
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        String guildId = args[0];
        String guildName = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
        String stat = AirUtils.insetIntoBlacklist(guildId, guildName, event.getJDA().getSelfUser().getId());

        if (!stat.isEmpty())
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + stat)).queue();
        else
            event.getChannel().sendMessage(AirUtils.embedMessage("Successfully added " + guildName + " to the blacklist")).queue();
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "add a guild to the whitelist";
    }
}
