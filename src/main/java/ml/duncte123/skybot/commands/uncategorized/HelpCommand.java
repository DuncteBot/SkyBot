/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import ml.duncte123.skybot.utils.HelpEmbeds;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class HelpCommand extends Command {

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (!ctx.getArgs().isEmpty()) {
            final String toSearch = ctx.getArgsRaw().toLowerCase()
                .replaceFirst("(" + Pattern.quote(Settings.PREFIX) + "|" +
                    Pattern.quote(Settings.OTHER_PREFIX) + "|" +
                    Pattern.quote(ctx.getGuildSettings().getCustomPrefix()) + ")", "");

            if (isCategory(toSearch)) {
                sendCategoryHelp(event, ctx.getGuild().getSettings().getCustomPrefix(), toSearch.toUpperCase());
                return;
            }

            sendCommandHelp(event, toSearch, ctx.getCommandManager());

            return;
        }
        sendHelp(event, HelpEmbeds.generateCommandEmbed(ctx.getGuildSettings().getCustomPrefix()));
    }

    @Override
    public String help() {
        return "Shows a list of all the commands.\nUsage: `" + Settings.PREFIX + "help [command]`";
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
            final List<CommandCategory> categoryList = Arrays.stream(CommandCategory.values()).filter(it -> it.getSearch()
                .equals(name.toLowerCase())).collect(Collectors.toList());

            if (!categoryList.isEmpty()) {
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
                err -> sendMsg(event,
                    "You can check out my commands here:\nhttps://bot.duncte123.me/commands?server=" +
                        event.getGuild().getId())
            ),
            err -> sendMsg(event, "ERROR: " + err.getMessage())
        );
    }

    private void sendCommandHelp(GuildMessageReceivedEvent event, String toSearch, CommandManager manager) {
        for (final ICommand cmd : manager.getCommands()) {
            if (cmd.getName().equals(toSearch)) {

                sendMsg(event, getCommandHelpMessage(cmd));

                return;
            }

            for (final String alias : cmd.getAliases()) {
                if (alias.equals(toSearch)) {
                    sendMsg(event, getCommandHelpMessage(cmd));

                    return;
                }
            }
        }

        sendMsg(event, "That command could not be found, try `" + Settings.PREFIX + "help` for a list of commands");
    }

    private String getCommandHelpMessage(ICommand cmd) {
        return "Command help for `" +
            cmd.getName() + "` :\n" + cmd.help(cmd.getName()) +
            (cmd.getAliases().length > 0 ? "\nAliases: " + String.join(", ", cmd.getAliases()) : "") +
            "\nSource code: <https://apis.duncte123.me/file/" + cmd.getClass().getSimpleName() + '>';
    }

    private void sendCategoryHelp(GuildMessageReceivedEvent event, String prefix, String toSearch) {
        final CommandCategory cat = getCategory(toSearch);
        final MessageEmbed embed = HelpEmbeds.generateCommandEmbed(prefix, cat);
        sendEmbed(event, embed);
    }

    private CommandCategory getCategory(String search) {

        try {
            return CommandCategory.valueOf(search.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            final List<CommandCategory> categoryList = Arrays.stream(CommandCategory.values()).filter(it -> it.getSearch()
                .equals(search.toLowerCase())).collect(Collectors.toList());
            return categoryList.get(0);
        }
    }
}
