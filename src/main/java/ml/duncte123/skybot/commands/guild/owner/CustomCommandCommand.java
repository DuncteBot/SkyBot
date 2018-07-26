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

import kotlin.Triple;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static ml.duncte123.skybot.utils.MessageUtils.*;
import static ml.duncte123.skybot.utils.Variables.COMMAND_MANAGER;

public class CustomCommandCommand extends Command {

    private final List<String> systemInvokes = List.of("add", "new", "edit", "change", "delete", "raw");

    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {

        if (args.length < 1) {
            sendMsg(event, "Insufficient arguments use `db!help customcommand`");
            return;
        }

        switch (args.length) {
            case 1:
                invokeCustomCommand(args[0], event);
                break;

            case 2:
                deleteOrShowCustomCommand(args, event);
                //sendMsg(event, "Insufficient arguments");
                break;

            default:
                addEditOrInvokeCustomCommand(args, event);
                break;
        }
    }

    private void invokeCustomCommand(String arg, GuildMessageReceivedEvent event) {
        if (arg.equalsIgnoreCase("list")) {
            GuildSettings s = getSettings(event.getGuild());
            StringBuilder sb = new StringBuilder();
            COMMAND_MANAGER.getCustomCommands().stream()
                    .filter(c -> c.getGuildId().equals(event.getGuild().getId()))
                    .forEach(cmd -> sb.append(s.getCustomPrefix())
                            .append(cmd.getName())
                            .append("\n")
                    );

            sendMsg(event, new MessageBuilder().append("Custom Commands for this server").append('\n')
                    .appendCodeBlock(sb.toString(), "ldif").build());
        } else {
            //fetch a custom command
            CustomCommand cmd = COMMAND_MANAGER.getCustomCommand(arg, event.getGuild().getId());
            if (cmd != null)
                //Run the custom command?
                COMMAND_MANAGER.dispatchCommand(cmd, arg, new String[0], event);
            else
                sendMsg(event, "Invalid arguments use `db!help customcommand`");
        }
    }

    private void deleteOrShowCustomCommand(String[] args, GuildMessageReceivedEvent event) {
        //Check for deleting
        if (args[0].equalsIgnoreCase("raw")) {
            final String commandName = args[1];
            final String guildid = event.getGuild().getId();

            if (!commandExists(commandName, guildid)) {
                sendMsg(event, "No command was found for this name");
                return;
            }
            CustomCommand cmd = COMMAND_MANAGER.getCustomCommand(commandName, guildid);
            sendMsg(event, "Raw data for `" + commandName + "`:```perl\n" + cmd.getMessage() + "```");
        } else if (args[0].equalsIgnoreCase("delete")) {

            if (!isAdmin(event)) {
                sendMsg(event, "You need the \"Administrator\" permission to add or remove commands");
                return;
            }

            final String commandName = args[1];
            final String guildid = event.getGuild().getId();

            if (!commandExists(commandName, guildid)) {
                sendMsg(event, "No command was found for this name");
                return;
            }

            boolean success = COMMAND_MANAGER.removeCustomCommand(commandName, guildid);
            Message msg = event.getMessage();
            if (!success) {
                sendErrorWithMessage(msg, "Failed to delete custom command.");
                return;
            }
            sendSuccess(msg);
        } else {
            sendMsg(event, "Invalid arguments use `db!help customcommand`");
        }
    }

    private void addEditOrInvokeCustomCommand(String[] args, GuildMessageReceivedEvent event) {

        if (!systemInvokes.contains(args[0])) {


            //fetch a custom command
            CustomCommand cmd = COMMAND_MANAGER.getCustomCommand(args[0], event.getGuild().getId());
            if (cmd != null)
                //Run the custom command?
                COMMAND_MANAGER.dispatchCommand(cmd, args[0], Arrays.copyOfRange(args, 1, args.length), event);

            return;

        }

        if (args.length < 3 && !systemInvokes.contains(args[0])) {
            sendMsg(event, "Invalid arguments use `db!help customcommand`");
            return;
        }

        if (!isAdmin(event)) {
            sendMsg(event, "You need the \"Administrator\" permission to add or remove commands");
            return;
        }
        //new command
        String commandName = args[1];

        if (commandName.length() > 10) {
            sendErrorWithMessage(event.getMessage(), "The maximum length of the command name is 10 characters");
            return;
        }

        String commandAction = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
        String guildId = event.getGuild().getId();
        if (commandExists(commandName, guildId)) {
            if (!args[0].equalsIgnoreCase("edit") && !args[0].equalsIgnoreCase("change")) {
                sendMsg(event, "A command already exists for this server.");
            } else {
                if (editCustomCommand(COMMAND_MANAGER.getCustomCommand(commandName, guildId), commandAction))
                    sendMsg(event, "The command has been updated.");
            }
            return;
        }
        Triple<Boolean, Boolean, Boolean> result = registerCustomCommand(commandName, commandAction, guildId);
        if (result.getFirst()) {
            sendMsg(event, "Command added.");
        } else {
            String error = "Failed to add custom command. \n Reason(s): %s";
            String reason = "";
            if (result.getSecond()) {
                reason += "The command was already found.\n";
            } else if (result.getThird()) {
                reason += "You reached the limit of 50 custom commands on this server.\n";
            } else if (!result.getSecond() && !result.getThird()) {
                reason += "We have an database issue.";
            }
            sendMsg(event, String.format(error, reason));
        }
    }

    private boolean commandExists(String name, String guild) {
        return COMMAND_MANAGER.getCustomCommand(name, guild) != null;
    }

    private Triple<Boolean, Boolean, Boolean> registerCustomCommand(String name, String action, String guildId) {
        return COMMAND_MANAGER.addCustomCommand(new CustomCommandImpl(name, action, guildId));
    }

    private boolean editCustomCommand(CustomCommand customCommand, String newMessage) {
        CustomCommand cmd = new CustomCommandImpl(customCommand.getName(), newMessage, customCommand.getGuildId());
        return COMMAND_MANAGER.editCustomCommand(cmd);
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAdmin(GuildMessageReceivedEvent event) {
        return event.getMember().hasPermission(event.getChannel(), Permission.ADMINISTRATOR);
    }
}
