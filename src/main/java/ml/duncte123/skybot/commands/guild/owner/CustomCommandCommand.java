/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.guild.owner;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static ml.duncte123.skybot.utils.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.MessageUtils.sendSuccess;

public class CustomCommandCommand extends Command {
    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {

        if(args.length < 1) {
            sendMsg(event, "Insufficient arguments");
            return;
        }

        switch (args.length) {
            case 1:
                argsLength1(args[0], event);
                break;

            case 2:
                argsLength2(args, event);
                //sendMsg(event, "Insufficient arguments");
                break;

            default:
                argsLengthOther(args, event);
                break;
        }
    }

    private void argsLength1(String arg, GuildMessageReceivedEvent event) {
        if ("list".equals(arg)) {
            GuildSettings s = getSettings(event.getGuild());
            StringBuilder sb = new StringBuilder();
            AirUtils.COMMAND_MANAGER.getCustomCommands().stream()
                    .filter(c -> c.getGuildId().equals(event.getGuild().getId())).forEach(cmd ->
                    sb.append(s.getCustomPrefix())
                            .append(cmd.getName())
                            .append("\n")
            );
            sendMsg(event, new MessageBuilder().append("Custom Commands for this server").append('\n')
                    .appendCodeBlock(sb.toString(), "ldif").build());
        } else {
            //fetch a custom command
            CustomCommand cmd = AirUtils.COMMAND_MANAGER.getCustomCommand(arg, event.getGuild().getId());
            //Run the custom command?
            AirUtils.COMMAND_MANAGER.dispatchCommand(((Command) cmd), arg, new String[0], event);
        }
    }

    private void argsLength2(String[] args, GuildMessageReceivedEvent event) {
        //Check for deleting
        if("delete".equals(args[0]) && isAdmin(event) ) {
            String commandName = args[1];
            String guildid = event.getGuild().getId();
            if(commandExists(commandName, guildid)) {
                AirUtils.COMMAND_MANAGER.removeCustomCommand(commandName, guildid);
                sendSuccess(event.getMessage());
            } else {
                sendMsg(event, "No command was found for this name");
            }

        } else {
            sendMsg(event, "You need the \"Manage Server\" permission to add or remove commands");
        }
    }

    private void argsLengthOther(String[] args, GuildMessageReceivedEvent event) {
        if(args.length >= 3) {

            if( ( "new".equals(args[0]) || "add".equals(args[0]) )  && isAdmin(event)) {
                //new command
                String commandName = args[1];

                if(commandName.length() > 10) {
                    MessageUtils.sendErrorWithMessage(event.getMessage(), "The maximum length of the command name is 10 characters");
                    return;
                }

                String commandAction = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                String guildId = event.getGuild().getId();
                if(!commandExists(commandName, guildId)) {
                    if(registerCustomCommand(commandName, commandAction, guildId)) {
                        sendMsg(event, "Command added");
                    } else {
                        sendMsg(event, "Could not add this command");
                    }
                } else {
                    sendMsg(event, "A command already exists for this server.");
                }
            } else {
                sendMsg(event, "You need the \"Manage Server\" permission to add or remove commands");
            }
        }
    }

    private boolean commandExists(String name, String guild) {
        return AirUtils.COMMAND_MANAGER.getCustomCommand(name, guild) != null;
    }

    private boolean registerCustomCommand(String name, String action, String guildId) {
        return AirUtils.COMMAND_MANAGER.addCustomCommand(new CustomCommandImpl( name, action, guildId ));
    }

    @Override
    public String help() {
        return "Create, run and delete custom commands\n" +
                "`" + PREFIX + getName() + " list` => Shows a list of all the custom commands\n" +
                "`" + PREFIX + getName() + " new <name> <text>` creates a new custom command\n" +
                "`" + PREFIX + getName() + " delete <name>` => deletes a custom command";
    }

    @Override
    public String getName() {
        return "customcommand";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"cc", "customcommands"};
    }

    private boolean isAdmin(GuildMessageReceivedEvent event) {
        return event.getMember().hasPermission(event.getChannel(), Permission.MANAGE_SERVER);
    }
}
