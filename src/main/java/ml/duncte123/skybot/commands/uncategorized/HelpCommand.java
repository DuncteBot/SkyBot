/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.*;
import ml.duncte123.skybot.utils.HelpEmbeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
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

    public HelpCommand() {
        this.name = "help";
        this.aliases = new String[]{
            "commandlist",
            "commands",
            "h",
        };
        this.helpFunction = (prefix, invoke) -> "Sends you a list of all the commands";
        this.usageInstructions = (prefix, invoke) -> '`' + prefix + invoke + " [command]`";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final String prefix = ctx.getPrefix();

        if (!ctx.getArgs().isEmpty()) {
            final String toSearch = ctx.getArgsRaw().toLowerCase()
                .replaceFirst("(" + Pattern.quote(Settings.PREFIX) + "|" +
                    Pattern.quote(Settings.OTHER_PREFIX) + "|" +
                    Pattern.quote(prefix) + ")", "");

            if (isCategory(toSearch)) {
                sendCategoryHelp(event, prefix, toSearch.toUpperCase());
                return;
            }

            sendCommandHelp(event, toSearch, ctx.getCommandManager(), prefix);

            return;
        }
        sendHelp(event, HelpEmbeds.generateCommandEmbed(prefix));
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isCategory(String name) {
        try {
            final List<CommandCategory> categoryList = Arrays.stream(CommandCategory.values()).filter(it -> name.toLowerCase()
                .equals(it.getSearch())).collect(Collectors.toList());

            if (!categoryList.isEmpty()) {
                return true;
            }

            return CommandCategory.valueOf(name.toUpperCase()) != null;
        }
        catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private void sendHelp(GuildMessageReceivedEvent event, MessageEmbed embed) {
        event.getAuthor()
            .openPrivateChannel()
            .flatMap((pc) -> pc.sendMessage(embed))
            .queue(
                (msg) -> sendMsg(event, event.getMember().getAsMention() + " check your DM's"),
                //When sending fails, send to the channel
                (err) -> sendMsg(event, "You can check out my commands here:\nhttps://dunctebot.com/commands")
            );
    }

    private void sendCommandHelp(GuildMessageReceivedEvent event, String toSearch, CommandManager manager, String prefix) {

        final ICommand cmd = manager.getCommand(toSearch);

        if (cmd != null) {
            sendCommandHelpMessage(event, (Command) cmd, prefix, toSearch);

            return;
        }

        sendMsg(event, "That command could not be found, try `" + prefix + "help` for a list of commands");
    }

    private void sendCommandHelpMessage(GuildMessageReceivedEvent event, Command cmd, String prefix, String invoke) {
        final Class<? extends Command> commandClass = cmd.getClass();
        final String clsPath = commandClass.getName().replace('.', '/');
        final boolean isKotlin = isKotlin(commandClass);
        final String extension = isKotlin ? ".kt" : ".java";
        final String type = isKotlin ? "kotlin" : "java";
        final String url = String.format("https://github.com/DuncteBot/SkyBot/blob/master/src/main/%s/%s%s", type, clsPath, extension);

        final EmbedBuilder builder = EmbedUtils
            .defaultEmbed()
            .setTitle("Command help for " + cmd.getName() + " (<required argument> [optional argument])", url)
            .setDescription(cmd.help(invoke, prefix) +
                "\nUsage: " + cmd.getUsageInstructions(invoke, prefix));

        if (cmd.flags.length > 0) {
            builder.addField("Flags", parseFlags(cmd.flags), false);
        }

        sendEmbed(event, builder);
    }

    private void sendCategoryHelp(GuildMessageReceivedEvent event, String prefix, String toSearch) {
        final CommandCategory cat = getCategory(toSearch);
        final MessageEmbed embed = HelpEmbeds.generateCommandEmbed(prefix, cat);
        sendEmbed(event, embed);
    }

    private String parseFlags(Flag[] flags) {
        final StringBuilder builder = new StringBuilder();

        for (final Flag flag : flags) {
            if (flag.getFlag() == null) {
                builder.append("`--")
                    .append(flag.getWord())
                    .append("` : ")
                    .append(flag.getDesc())
                    .append('\n');
            } else if (flag.getWord() == null) {
                builder.append("`-")
                    .append(flag.getFlag())
                    .append("` : ")
                    .append(flag.getDesc())
                    .append('\n');
            } else {
                builder.append("`-")
                    .append(flag.getFlag())
                    .append("/--")
                    .append(flag.getWord())
                    .append("` : ")
                    .append(flag.getDesc())
                    .append('\n');
            }
        }

        return builder.toString();
    }

    private boolean isKotlin(Class<? extends Command> klass) {
        return Arrays.stream(klass.getDeclaredAnnotations())
            .anyMatch((c) -> c.annotationType().getName().equals("kotlin.Metadata"));
    }

    private CommandCategory getCategory(String search) {
        return CommandCategory.fromSearch(search);
    }
}
