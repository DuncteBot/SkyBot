package ml.duncte123.skybot.commands.guild.owner;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.Settings;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SettingsCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if(!PermissionUtil.checkPermission(event.getMember(), Permission.ADMINISTRATOR)) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        GuildSettings settings = getSettings(event.getGuild().getId());

        if(args.length < 1) {
            //true ✅
            //false ❌
            MessageEmbed message = EmbedUtils.embedMessage("Here are the settings from this guild.\n" +
                            "**Show join messages:** " + (settings.isEnableJoinMessage() ? "✅" : "❌") + "\n" +
                            "**Swearword filter:** " + (settings.isEnableSwearFilter() ? "✅" : "❌") + "\n" +
                            "**Join message:** " + settings.getCustomJoinMessage() + "\n" +
                            "**Current prefix:** " + settings.getCustomPrefix()
            );
            sendEmbed(message, event);
        } else if(args.length == 1) {
            sendMsg(event, "Incorrect usage: `" + Settings.prefix + "settings [module] [status/options]`");
        } else {
            List<String> modules = Arrays.asList("showJoinMessage", "swearFilter", "setJoinMessage", "setPrefix");
            String module = args[0];
            if(modules.contains(module)) {
                switch (module) {
                    case "showJoinMessage" :
                        GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setEnableJoinMessage(checkStatus(args[1])));
                        break;
                    case "swearFilter":
                        GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setEnableSwearFilter(checkStatus(args[1])));
                        break;
                    case "setJoinMessage":
                        String newJoinMsg = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
                        GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomJoinMessage(newJoinMsg));
                        break;
                    case "setPrefix":
                        String newPrefix = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
                        GuildSettingsUtils.updateGuildSettings(event.getGuild(), settings.setCustomPrefix(newPrefix));
                        break;

                    default:
                        return;
                }
                sendEmbed(EmbedUtils.embedMessage("Settings have been updated."), event);

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

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"options"};
    }

    private boolean checkStatus(String toCHeck) {
        try {
            return (Integer.parseInt(toCHeck) >= 1);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
