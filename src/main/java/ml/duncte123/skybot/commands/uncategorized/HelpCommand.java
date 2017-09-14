package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import ml.duncte123.skybot.utils.HelpEmbeds;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class HelpCommand extends Command {

    public final static String help = "shows a list of all the commands.";

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if(args.length > 0) {
            String toSearch = StringUtils.join(args, " ");

            for(Command cmd : AirUtils.commandSetup.getCommands()) {
                if(cmd.getName().equals(toSearch)) {
                    sendMsg(event, "Command help for "+toSearch+" :\n" + cmd.help());
                    return;
                }
            }

            sendMsg(event, "That command could not be found, try "+ Config.prefix+"help for a list of commands");
            return;
        }

        event.getAuthor().openPrivateChannel().queue(
            pc -> pc.sendMessage(HelpEmbeds.commandList).queue(
                 msg ->  event.getChannel().sendMessage(event.getMember().getAsMention() +" check your DM's").queue(),
                //When sending fails, send to the channel
                err -> event.getChannel().sendMessage(HelpEmbeds.commandList).complete().getChannel().sendMessage("Message could not be delivered to dm's and has been send in this channel.").queue()
            ),
            err -> event.getChannel().sendMessage("ERROR: " + err.getMessage()).queue()
        );
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

    @Override
    public String getName() {
        return "help";
    }
}
