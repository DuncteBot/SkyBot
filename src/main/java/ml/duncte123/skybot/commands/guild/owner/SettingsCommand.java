package ml.duncte123.skybot.commands.guild.owner;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class SettingsCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {

        if(!PermissionUtil.checkPermission(event.getMember(), Permission.ADMINISTRATOR)) {
            sendMsg(event, "You don't have permission to run this command");
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

        GuildSettings settings = AirUtils.guildSettings.get(event.getGuild().getId());

        if(args.length < 1) {
            //true ✅
            //false ❌
            MessageEmbed message = AirUtils.embedMessage("Here are the settings from this guild.\n" +
                            "Join messages: " + (settings.isEnableJoinMessage() ? "✅" : "❌") + "\n" +
                            "Swearword filter: " + (settings.isEnableSwearFilter() ? "✅" : "❌")
            );
            return;
        }

    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Modify the settings on the bot";
    }
}
