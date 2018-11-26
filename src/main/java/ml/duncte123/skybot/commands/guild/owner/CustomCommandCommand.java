/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.*;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class CustomCommandCommand extends Command {

    private final List<String> systemInvokes = List.of("add", "new", "edit", "change", "delete", "raw");

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgsWithQuotes();
        CommandManager manager = ctx.getCommandManager();

        if (args.isEmpty()) {
            sendMsg(event, "Insufficient arguments use `db!help customcommand`");
            return;
        }

        switch (args.size()) {
            case 1:
                invokeCustomCommand(args.get(0), event, manager, ctx);
                break;

            case 2:
                deleteOrShowCustomCommand(args, event, manager);
                //sendMsg(event, "Insufficient arguments");
                break;

            default:
                addEditOrInvokeCustomCommand(args, event, manager);
                break;
        }
    }

    private void invokeCustomCommand(String arg, GuildMessageReceivedEvent event, CommandManager manager, CommandContext ctx) {
        if (arg.equalsIgnoreCase("list")) {
            GuildSettings s = ctx.getGuildSettings();
            StringBuilder sb = new StringBuilder();
            manager.getCustomCommands().stream()
                .filter(c -> c.getGuildId() == event.getGuild().getIdLong())
                .forEach(cmd -> sb.append(s.getCustomPrefix())
                    .append(cmd.getName())
                    .append("\n")
                );

            sendMsg(event, new MessageBuilder().append("Custom Commands for this server").append('\n')
                .appendCodeBlock(sb.toString(), "ldif").build());
        } else {
            //fetch a custom command
            CustomCommand cmd = manager.getCustomCommand(arg, event.getGuild().getIdLong());
            if (cmd != null) {
                //Run the custom command?
                manager.dispatchCommand(cmd, arg, List.of(), event);
            } else {
                sendMsg(event, "Invalid arguments use `db!help customcommand`");
            }
        }
    }

    private void deleteOrShowCustomCommand(List<String> args, GuildMessageReceivedEvent event, CommandManager manager) {
        //Check for deleting
        if (args.get(0).equalsIgnoreCase("raw")) {
            final String commandName = args.get(1);
            final long guildid = event.getGuild().getIdLong();

            if (!commandExists(commandName, guildid, manager)) {
                sendMsg(event, "No command was found for this name");
                return;
            }
            CustomCommand cmd = manager.getCustomCommand(commandName, guildid);
            sendMsg(event, "Raw data for `" + commandName + "`:```perl\n" + cmd.getMessage() + "```");
        } else if (args.get(0).equalsIgnoreCase("delete")) {

            if (!isAdmin(event)) {
                sendMsg(event, "You need the \"Administrator\" permission to add or remove commands");
                return;
            }

            final String commandName = args.get(1);
            final long guildid = event.getGuild().getIdLong();

            if (!commandExists(commandName, guildid, manager)) {
                sendMsg(event, "No command was found for this name");
                return;
            }

            boolean success = manager.removeCustomCommand(commandName, guildid);
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

    private void addEditOrInvokeCustomCommand(List<String> args, GuildMessageReceivedEvent event, CommandManager manager) {

        if (!systemInvokes.contains(args.get(0))) {


            //fetch a custom command
            CustomCommand cmd = manager.getCustomCommand(args.get(0), event.getGuild().getIdLong());
            if (cmd != null) {
                //Run the custom command?
                manager.dispatchCommand(cmd, args.get(0), args.subList(1, args.size()), event);
            }

            return;

        }

        if (args.size() < 3 && !systemInvokes.contains(args.get(0))) {
            sendMsg(event, "Invalid arguments use `db!help customcommand`");
            return;
        }

        if (!isAdmin(event)) {
            sendMsg(event, "You need the \"Administrator\" permission to add or remove commands");
            return;
        }
        //new command
        String commandName = args.get(1);

        if (commandName.length() > 25) {
            sendErrorWithMessage(event.getMessage(), "The maximum length of the command name is 25 characters");
            return;
        }

        String commandAction = StringUtils.join(args.subList(2, args.size()), " ");
        long guildId = event.getGuild().getIdLong();
        if (commandExists(commandName, guildId, manager)) {

            if (!args.get(0).equalsIgnoreCase("edit") && !args.get(0).equalsIgnoreCase("change")) {
                sendMsg(event, "A command already exists for this server.");

                return;
            }
            if (editCustomCommand(manager.getCustomCommand(commandName, guildId), commandAction, manager)) {
                sendMsg(event, "The command has been updated.");
                return;
            }

            return;
        }
        Triple<Boolean, Boolean, Boolean> result = registerCustomCommand(commandName, commandAction, guildId, manager);

        if (result.getFirst()) {
            sendMsg(event, "Command added.");

            return;
        }
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

    public static boolean commandExists(String name, long guild, CommandManager manager) {
        return manager.getCustomCommand(name, guild) != null;
    }

    public static Triple<Boolean, Boolean, Boolean> registerCustomCommand(String name, String action, long guildId, CommandManager manager) {
        return manager.addCustomCommand(new CustomCommandImpl(name, action, guildId));
    }

    public static boolean editCustomCommand(CustomCommand customCommand, String newMessage, CommandManager manager) {
        CustomCommand cmd = new CustomCommandImpl(customCommand.getName(), newMessage, customCommand.getGuildId());
        return manager.editCustomCommand(cmd);
    }

    @Override
    public String help() {
        return "Create, run and delete custom commands\n" +
            "`" + PREFIX + getName() + " list` => Shows a list of all the custom commands\n" +
            "`" + PREFIX + getName() + " new <name> <text>` => Creates a new custom command\n" +
            "`" + PREFIX + getName() + " edit <name> <text>` => Edits a custom command\n" +
            "`" + PREFIX + getName() + " raw <name>` => Shows the raw value of a custom command\n" +
            "`" + PREFIX + getName() + " delete <name>` => Deletes a custom command";
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
