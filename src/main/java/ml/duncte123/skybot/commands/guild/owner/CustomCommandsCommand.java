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

import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static ml.duncte123.skybot.utils.MessageUtils.sendMsg;

public class CustomCommandsCommand extends Command {
    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {
        //noinspection deprecation
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER) && !event.getAuthor().getId().equals(Settings.wbkxwkZPaG4ni5lm8laY[0])) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        if(args.length < 1) {
            sendMsg(event, "Insufficient arguments");
            return;
        }

        GuildSettings gs = getSettings(event.getGuild());
        switch (args.length) {
            case 1:
                argsLength1(gs, args[0], event);
                break;

            case 2:
                argsLength2(gs, args, event);
                //sendMsg(event, "Insufficient arguments");
                break;

            default:
                argsLengthOther(gs, args, event);
                break;
        }
    }

    private void argsLength1(GuildSettings s, String arg, GuildMessageReceivedEvent event) {
        if ("list".equals(arg)) {
            StringBuilder sb = new StringBuilder();
            AirUtils.COMMAND_MANAGER.customCommands.stream()
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

    private void argsLength2(GuildSettings s, String[] args, GuildMessageReceivedEvent event) {
        //CHeck for deleting
    }

    private void argsLengthOther(GuildSettings s, String[] args, GuildMessageReceivedEvent event) {
        if(args.length >= 3) {

            if("new".equals(args[0])) {
                //new command
                String commandName = args[1];
                String commandAction = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                String guildId = event.getGuild().getId();
                if(!commandAlreadyExists(commandName, guildId)) {
                    if(registerCustomCommand(commandName, commandAction, guildId)) {
                        sendMsg(event, "Command added");
                    } else {
                        sendMsg(event, "Could not add this command");
                    }
                } else {
                    sendMsg(event, "A command already exists for this server.");
                }
            }

        }
    }

    private boolean commandAlreadyExists(String name, String guild) {
        return AirUtils.COMMAND_MANAGER.getCustomCommand(name, guild) != null;
    }

    private boolean registerCustomCommand(String name, String action, String guildId) {
        return AirUtils.COMMAND_MANAGER.addCustomCommand(new CustomCommandImpl( name, action, guildId ));
    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public String getName() {
        return "customcommand";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"cc", "customcommands"};
    }
}
