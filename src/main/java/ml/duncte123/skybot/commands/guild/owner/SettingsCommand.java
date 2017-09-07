package ml.duncte123.skybot.commands.guild.owner;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SettingsCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {

        if(event.getAuthor().getId().equals(Config.ownerId)) {
            return true;
        }

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
                            "Show join messages: " + (settings.isEnableJoinMessage() ? "✅" : "❌") + "\n" +
                            "Swearword filter: " + (settings.isEnableSwearFilter() ? "✅" : "❌")
            );
            sendMsg(event, message);
            return;
        }

        if(args.length > 2) {
            sendMsg(event, "Incorrect usage: `" + Config.prefix + "settings [module] [status]`");
            return;
        }

        if(args.length > 1) {
            List<String> modules = Arrays.asList("showJoinMessage", "swearFilter");
            String module = args[0];
            boolean enableStatus;
            try {
                enableStatus = (Integer.parseInt(args[1]) >= 1);
            }
            catch (NumberFormatException e) {
                sendMsg(event, "Incorrect usage, status must be either 0 (to disable) or 1 (to enable)");
                return;
            }
            if(modules.contains(module)) {
                switch (module) {
                    case "showJoinMessage" :
                        AirUtils.updateGuildSettings(settings.setEnableJoinMessage(enableStatus));
                        sendMsg(event, AirUtils.embedMessage("Settings have been updated."));
                        break;
                    case "swearFilter":
                        AirUtils.updateGuildSettings(settings.setEnableSwearFilter(enableStatus));
                        sendMsg(event, AirUtils.embedMessage("Settings have been updated."));
                        break;

                    default:
                        break;
                }

            } else {
                sendMsg(event, "Module has not been reconsigned, please choose from: `" + StringUtils.join(modules, ", ") + "`");
            }
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
