/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.guild.owner;

import com.dunctebot.models.settings.GuildSetting;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.objects.command.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.*;

// TODO: trash this command, it is a clutter and hell to maintain
public class CustomCommandCommand extends Command {
    private final List<String> systemInvokes = List.of("add", "new", "edit", "change", "delete", "remove", "raw");

    public CustomCommandCommand() {
        this.requiresArgs = true;
        this.category = CommandCategory.ADMINISTRATION;
        this.name = "customcommand";
        this.aliases = new String[]{
            "cc",
            "customcommands",
        };
        this.help = "Create, edit and delete custom commands";
        this.usage = "<list/new/edit/raw/delete> [name] [text]";
        this.extraInfo = "• `{prefix}" + this.name + "list` => Shows a list of all the custom commands\n" +
            "• `{prefix}" + this.name + " new <name> <text>` => Creates a new custom command\n" +
            "• `{prefix}" + this.name + " edit <name> <text>` => Edits a custom command\n" +
            "• `{prefix}" + this.name + " raw <name>` => Shows the raw value of a custom command\n" +
            "• `{prefix}" + this.name + " delete <name>` => Deletes a custom command";
    }


    @SuppressWarnings("PMD.SwitchStmtsShouldHaveDefault") // SIGH, it has a default
    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgsWithQuotes();

        if (args.size() > 1 && !ctx.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            sendMsg(ctx, "You need the \"Administrator\" permission to modify commands");
            return;
        }

        final CommandManager manager = ctx.getCommandManager();

        switch (args.size()) {
            case 1 -> listCustomCommands(args.get(0), ctx, manager);
            case 2 -> deleteOrShowCustomCommand(args, ctx, manager, ctx.getPrefix());
            default -> addOrEditCustomCommand(args, ctx, manager, ctx.getPrefix());
        }
    }

    private void listCustomCommands(String arg, CommandContext ctx, CommandManager manager) {
        if ("list".equalsIgnoreCase(arg)) {
            final GuildSetting setting = ctx.getGuildSettings();
            final StringBuilder builder = new StringBuilder();

            manager.getCustomCommands().stream()
                .filter(c -> c.getGuildId() == ctx.getGuild().getIdLong())
                .forEach(cmd -> builder.append(setting.getCustomPrefix())
                    .append(cmd.getName())
                    .append('\n')
                );

            sendMsg(ctx, "Custom Commands for this server\n```ldif\n"+ builder.toString() + "\n```");
        } else {
            sendMsg(ctx, "Insufficient arguments use `" + ctx.getPrefix() + "help customcommand`");
        }
    }

    private void deleteOrShowCustomCommand(List<String> args, CommandContext ctx, CommandManager manager, String prefix) {
        final String commandName = args.get(1);
        final long guildId = ctx.getGuild().getIdLong();

        //Check for deleting
        if ("raw".equalsIgnoreCase(args.get(0))) {
            if (!commandExists(commandName, guildId, manager)) {
                sendMsg(ctx, "No command was found for this name");
                return;
            }

            final CustomCommand cmd = manager.getCustomCommand(commandName, guildId);
            final String escaped = cmd.getMessage().replaceAll("`", "");
            sendMsg(ctx, "Raw data for `" + commandName + "`:```pascal\n" + escaped + "\n```");
        } else if ("delete".equalsIgnoreCase(args.get(0)) || "remove".equalsIgnoreCase(args.get(0))) {

            if (!commandExists(commandName, guildId, manager)) {
                sendMsg(ctx, "No command was found for this name");
                return;
            }

            final boolean success = manager.removeCustomCommand(commandName, guildId);
            final Message msg = ctx.getMessage();
            if (!success) {
                sendErrorWithMessage(msg, "Failed to delete custom command.");
                return;
            }
            sendSuccess(msg);
        } else {
            sendMsg(ctx, "Invalid arguments use `" + prefix + "help customcommand`");
        }
    }

    private void addOrEditCustomCommand(List<String> args, CommandContext ctx, CommandManager manager, String prefix) {
        if (args.size() < 3 && !systemInvokes.contains(args.get(0))) {
            sendMsg(ctx, "Invalid arguments use `" + prefix + "help customcommand`");
            return;
        }

        //new command
        final String commandName = args.get(1);

        if (commandName.length() > 25) {
            sendErrorWithMessage(ctx.getMessage(), "The maximum length of the command name is 25 characters");
            return;
        }

        final String commandAction = String.join(" ", args.subList(2, args.size()));
        final long guildId = ctx.getGuild().getIdLong();

        if (commandExists(commandName, guildId, manager)) {
            editCustomCommand(args, ctx, manager, commandName, commandAction, guildId);

            return;
        }

        createCustomCommand(ctx, manager, commandName, commandAction, guildId);
    }

    private void createCustomCommand(CommandContext ctx, CommandManager manager, String commandName, String commandAction, long guildId) {
        try {
            final CommandResult result = registerCustomCommand(commandName, commandAction, guildId, manager);

            if (result == CommandResult.SUCCESS) {
                sendMsg(ctx, "Command added.");

                return;
            }

            final String error = "Failed to add custom command. \n Reason(s): %s";
            String reason = "";

            if (result == CommandResult.COMMAND_EXISTS) {
                reason += "The command was already found.\n";
            } else if (result == CommandResult.LIMIT_REACHED) {
                reason += "You reached the limit of 50 custom commands on this server.\n";
            } else {
                reason += "We have an database issue.";
            }

            sendMsg(ctx, String.format(error, reason));
        }
        catch (IllegalArgumentException e) {
            sendMsg(ctx, "Could not add command: " + e.getMessage());
        }
    }

    private void editCustomCommand(List<String> args, CommandContext ctx, CommandManager manager, String commandName, String commandAction, long guildId) {
        if (!"edit".equalsIgnoreCase(args.get(0)) && !"change".equalsIgnoreCase(args.get(0))) {
            sendMsg(ctx, "A command already exists for this server.");

            return;
        }

        if (editCustomCommand(manager.getCustomCommand(commandName, guildId), commandAction, manager)) {
            sendMsg(ctx, "The command has been updated.");
        }
    }

    private CommandResult registerCustomCommand(String name, String action, long guildId, CommandManager manager) {
        return manager.registerCustomCommand(new CustomCommand(name, action, guildId, false));
    }

    private boolean editCustomCommand(@Nullable CustomCommand customCommand, String newMessage, CommandManager manager) {
        if (customCommand == null) {
            return false;
        }

        final CustomCommand cmd = new CustomCommand(
            customCommand.getName(),
            newMessage,
            customCommand.getGuildId(),
            false
        );

        return manager.editCustomCommand(cmd);
    }

    private boolean commandExists(String name, long guild, CommandManager manager) {
        return manager.getCustomCommand(name, guild) != null;
    }
}
