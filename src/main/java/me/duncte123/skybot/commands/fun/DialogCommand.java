package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class DialogCommand implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        if (args.length < 1) {
            event.getChannel().sendMessage("Type some words please").queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
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

    @Override
    public void executed(boolean safe, MessageReceivedEvent event) {
        return;
    }
}
