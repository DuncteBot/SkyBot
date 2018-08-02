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

package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.HelpEmbeds;
import ml.duncte123.skybot.utils.Variables;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ml.duncte123.skybot.utils.MessageUtils.sendEmbed;
import static ml.duncte123.skybot.utils.MessageUtils.sendMsg;

public class HelpCommand extends Command {

    @SuppressWarnings("NullableProblems")
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();

        if (ctx.getArgs().size() > 0) {
            String toSearch = ctx.getRawArgs().toLowerCase()
                    .replaceFirst("(" + Pattern.quote(PREFIX) + "|" +
                            Pattern.quote(Settings.OTHER_PREFIX) + "|" +
                            Pattern.quote(ctx.getGuildSettings().getCustomPrefix()) + ")", "");

            if (isCategory(toSearch))
                sendCategoryHelp(event, toSearch.toUpperCase());
            else
                sendCommandHelp(event, toSearch);

            return;
        }
        sendHelp(event, HelpEmbeds.getCommandListWithPrefix(GuildSettingsUtils.getGuild(event.getGuild()).getCustomPrefix()));
    }

    @Override
    public String help() {
        return "Shows a list of all the commands.\nUsage: `" + PREFIX + "help [command]`";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"commands"};
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isCategory(String name) {
        try {
            List<CommandCategory> categoryList = Arrays.stream(CommandCategory.values()).filter(it -> it.getSearch()
                    .equals(name.toLowerCase())).collect(Collectors.toList());
            if (categoryList.size() > 0) {
                return true;
            }

            return CommandCategory.valueOf(name.toUpperCase()) != null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private void sendHelp(GuildMessageReceivedEvent event, MessageEmbed embed) {
        event.getAuthor().openPrivateChannel().queue(
                pc -> pc.sendMessage(embed).queue(
                        msg -> sendMsg(event, event.getMember().getAsMention() + " check your DM's"),
                        //When sending fails, send to the channel
                        err -> sendMsg(event, (new MessageBuilder())
                                .append("Message could not be delivered to dm's and has been send in this channel.")
                                .setEmbed(embed).build())
                ),
                err -> sendMsg(event, "ERROR: " + err.getMessage())
        );
    }

    private void sendCommandHelp(GuildMessageReceivedEvent event, String toSearch) {
        for (ICommand cmd : Variables.COMMAND_MANAGER.getCommands()) {
            if (cmd.getName().equals(toSearch)) {
                sendMsg(event, "Command help for `" +
                        cmd.getName() + "` :\n" + cmd.help(cmd.getName()) +
                        (cmd.getAliases().length > 0 ? "\nAliases: " + StringUtils.join(cmd.getAliases(), ", ") : ""));
                return;
            } else {
                for (String alias : cmd.getAliases()) {
                    if (alias.equals(toSearch)) {
                        sendMsg(event, "Command help for `" + cmd.getName() + "` :\n" +
                                cmd.help(alias) + (cmd.getAliases().length > 0 ? "\nAliases: "
                                + StringUtils.join(cmd.getAliases(), ", ") : ""));
                        return;
                    }

                }

            }
        }

        sendMsg(event, "That command could not be found, try " + PREFIX + "help for a list of commands");
    }

    private void sendCategoryHelp(GuildMessageReceivedEvent event, String toSearch) {
        CommandCategory cat = getCategory(toSearch);
        MessageEmbed embed = HelpEmbeds.getCommandListWithPrefix(GuildSettingsUtils.getGuild(event.getGuild()).getCustomPrefix(), cat);
        sendEmbed(event, embed);
    }

    private CommandCategory getCategory(String search) {

        try {
            return CommandCategory.valueOf(search.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            List<CommandCategory> categoryList = Arrays.stream(CommandCategory.values()).filter(it -> it.getSearch()
                    .equals(search.toLowerCase())).collect(Collectors.toList());
            return categoryList.get(0);
        }
    }
}
