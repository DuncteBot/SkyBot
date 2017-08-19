package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class DialogCommand extends Command {

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {

        if (args.length < 1) {
            event.getChannel().sendMessage("Type some words please").queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        StringBuilder sb = new StringBuilder()
                .append("```")
                .append("╔═══════════════════════════╗ \n")
                .append("║ Alert                     ║\n")
                .append("╠═══════════════════════════╣\n")
                .append("║ " + StringUtils.join(args, " ").replaceAll("```", "").replaceAll("`", "") + " ║\n")
                .append("║  ┌─────────┐  ┌────────┐  ║\n")
                .append("║  │   Yes   │  │   No   │  ║\n")
                .append("║  └─────────┘  └────────┘  ║\n")
                .append("╚═══════════════════════════╝\n")
                .append("```");
        event.getChannel().sendMessage(AirUtils.embedMessage(sb.toString())).queue();

    }

    @Override
    public String help() {
        return "Gives you a nice dialog";
    }
}
