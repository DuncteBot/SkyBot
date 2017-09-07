package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class CoinCommand extends Command {

    public final static String help = "flips a coin.";
    /**
     * this is where the coins are stored
     */
    private final String coinUrl = "https://dshelmondgames.ml/img/coin/";
    /**
     * this are our images
     */
    private final String[] imagesArr = { "heads.png", "tails.png" };
    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {

        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessage("*Flips a coin*").queue();
        MessageEmbed eb = AirUtils.embedImage(coinUrl+imagesArr[AirUtils.rand.nextInt(2)]);
        if(!PermissionUtil.checkPermission(event.getGuild().getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
            event.getChannel().sendMessage(AirUtils.embedToMessage(eb)).queue();
            return;
        }
        event.getChannel().sendMessage(eb).queue();
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }
}
