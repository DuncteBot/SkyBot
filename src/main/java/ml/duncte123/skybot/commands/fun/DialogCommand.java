package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.commands.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class DialogCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {

        if (args.length < 1) {
            event.getChannel().sendMessage("Type some words please").queue();
            return false;
        }

        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {

        String lines = StringUtils.join(args, " ").replaceAll("`", "");

        StringBuilder sb = new StringBuilder()
                .append("```")
                .append("╔═══════════════════════════╗ \n")
                .append("║ Alert                     ║\n")
                .append("╠═══════════════════════════╣\n")
                .append("║ " + String.format("%-25s", lines) + " ║\n") //Thnx Kantenkugel#1568 you're awesome
                .append("║  ┌─────────┐  ┌────────┐  ║\n")
                .append("║  │   Yes   │  │   No   │  ║\n")
                .append("║  └─────────┘  └────────┘  ║\n")
                .append("╚═══════════════════════════╝\n")
                .append("```");
        event.getChannel().sendMessage(AirUtils.embedMessage(sb.toString())).queue();

    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Gives you a nice dialog";
    }
}
