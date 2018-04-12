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
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import static ml.duncte123.skybot.utils.MessageUtils.sendMsg;

public class CustomCommandsCommand extends Command {
    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {
        //noinspection deprecation
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER) && !event.getAuthor().getId().equals(Settings.wbkxwkZPaG4ni5lm8laY[0])) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        if (args.length < 1) {
            sendMsg(event, "Insufficient arguments");
            return;
        }
        GuildSettings gs = getSettings(event.getGuild());
        switch (args.length) {
            case 1:
                argsLength1(gs, args[0], event);
                break;

            default:
                sendMsg(event, "Insufficient arguments");
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
            sendMsg(event, new MessageBuilder().appendCodeBlock(sb.toString(), "ldif").build());
        } else {
            //fetch a custom command
            CustomCommand cmd = AirUtils.COMMAND_MANAGER.getCustomCommand(arg, event.getGuild().getId());
            //Run the custom command?
            AirUtils.COMMAND_MANAGER.dispatchCommand(((Command) cmd), arg, new String[0], event);
        }
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
