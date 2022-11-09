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

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.duncte123.botcommons.messaging.EmbedUtils.getDefaultEmbed;
import static ml.duncte123.skybot.Settings.PATREON;

@SuppressWarnings("PMD")
public class HelpEmbeds {
    /// <editor-fold desc="Command storage" defaultstate="collapsed">
    private static String MAIN_COMMANDS = "";
    private static String MUSIC_COMMANDS = "";
    private static String ANIMALS_COMMANDS = "";
    private static String WEEB_COMMANDS = "";
    private static String LGBTQ_COMMANDS = "";
    private static String FUN_COMMANDS = "";
    private static String UTILS_COMMANDS = "";
    private static String MODERATION_COMMANDS = "";
    private static String ADMINISTRATION_COMMANDS = "";
    private static String PATRON_COMMANDS = "";
    private static String NSFW_COMMANDS = "";
    /// </editor-fold>

    private static final boolean INLINE = false;

    public static EmbedBuilder generateCommandEmbed(@Nonnull String prefix, @Nullable CommandCategory category) {
        final EmbedBuilder embed = getDefaultEmbed()
            .setTitle("Use `" + prefix + "help [command/category]` to get more info about a command\n", PATREON);

        if (category == null) {
            addAllCategoriesToEmbed(embed);
        } else {
            embed.addField(getFieldForCategory(category));
        }

        return embed.addField("Important links",
            "Discord server: [duncte.bot/server](https://duncte.bot/server)\n" +
                "Support development of this bot: [" + PATREON + "](" + PATREON + ")\n" +
                "Privacy policy: [duncte.bot/privacy](https://duncte.bot/privacy)", false);
    }

    /// <editor-fold desc="Reflection magic" defaultstate="collapsed">
    public static void init(final CommandManager manager) {
        final CommandCategory[] categories = CommandCategory.values();

        for (final CommandCategory category : categories) {
            if (category == CommandCategory.UNLISTED) {
                continue;
            }

            try {
                final List<String> cmds = new ArrayList<>();
                final List<ICommand<CommandContext>> foundCommands = manager.getCommands(category);

                for (final ICommand command : foundCommands) {
                    cmds.add(command.getName());

                    if (command.shouldDisplayAliasesInHelp()) {
                        cmds.addAll(Arrays.asList(command.getAliases()));
                    }
                }

                final String joinedCommands = joinCommands(cmds);

                switch (category) {
                    case ANIMALS -> ANIMALS_COMMANDS = joinedCommands;
                    case MAIN -> MAIN_COMMANDS = joinedCommands;
                    case FUN -> FUN_COMMANDS = joinedCommands;
                    case MUSIC -> MUSIC_COMMANDS = joinedCommands;
                    case MODERATION -> MODERATION_COMMANDS = joinedCommands;
                    case ADMINISTRATION -> ADMINISTRATION_COMMANDS = joinedCommands;
                    case UTILS -> UTILS_COMMANDS = joinedCommands;
                    case PATRON -> PATRON_COMMANDS = joinedCommands;
                    case WEEB -> WEEB_COMMANDS = joinedCommands;
                    case NSFW -> NSFW_COMMANDS = joinedCommands;
                    case LGBTQ -> LGBTQ_COMMANDS = joinedCommands;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private static void addAllCategoriesToEmbed(EmbedBuilder embed) {
        final CommandCategory[] categories = CommandCategory.values();

        for (final CommandCategory category : categories) {
            if (category == CommandCategory.UNLISTED) {
                continue;
            }

            embed.addField(getFieldForCategory(category));
        }
    }

    private static MessageEmbed.Field getFieldForCategory(CommandCategory category) {
        final String commands = switch (category) {
            case ANIMALS -> ANIMALS_COMMANDS;
            case MAIN -> MAIN_COMMANDS;
            case FUN -> FUN_COMMANDS;
            case MUSIC -> MUSIC_COMMANDS;
            case MODERATION -> MODERATION_COMMANDS;
            case ADMINISTRATION -> ADMINISTRATION_COMMANDS;
            case UTILS -> UTILS_COMMANDS;
            case PATRON -> PATRON_COMMANDS;
            case WEEB -> WEEB_COMMANDS;
            case NSFW -> NSFW_COMMANDS;
            case LGBTQ -> LGBTQ_COMMANDS;
            case UNLISTED -> null;
        };

        if (commands == null) {
            return null;
        }

        return new MessageEmbed.Field(
            category.getDisplay() + " Commands",
            commands,
            INLINE
        );
    }
    /// </editor-fold>

    private static String joinCommands(List<String> cmdNames) {
        return "`" + String.join("` | `", cmdNames) + "`";
    }
}
