package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.net.URL;

public class SealCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        try {
            int availableSeals = 83;
            int sealID = (int) Math.floor(Math.random() * availableSeals) + 1;
            String idStr = ("0000" + String.valueOf(sealID)).substring(String.valueOf(sealID).length());
            String sealLoc = "https://raw.githubusercontent.com/TheBITLINK/randomse.al/master/seals/" + idStr + ".jpg";

            if (event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
                event.getChannel().sendFile(new URL(sealLoc).openStream(), "Seal_"+System.currentTimeMillis()+".jpg", null).queue();
            } else {
                sendMsg(event, sealLoc);
            }
        }
        catch (Exception e) {
            sendMsg(event, "ERROR: "+e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Here is a nice seal";
    }

    @Override
    public String getName() {
        return "seal";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"zeehond"};
    }
}
