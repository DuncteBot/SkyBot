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

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.duncte123.botcommons.messaging.EmbedUtils.getDefaultEmbed;

@SuppressWarnings({"FieldCanBeLocal", "unused", "FieldMayBeFinal", "PMD"})
@Author(nickname = "duncte123", author = "Duncan Sterken")
public class HelpEmbeds {
    /// <editor-fold desc="Command storage" defaultstate="collapsed">
    private static String MAIN_COMMANDS = "";
    private static final String MAIN_COMMANDS_DESC = "Main commands";
    private static String MUSIC_COMMANDS = "";
    private static final String MUSIC_COMMANDS_DESC = "Music commands";
    private static String ANIMALS_COMMANDS = "";
    private static final String ANIMALS_COMMANDS_DESC = "Animal commands";
    private static String WEEB_COMMANDS = "";
    private static final String WEEB_COMMANDS_DESC = "Weeb commands";
    private static String LGBTQ_COMMANDS = "";
    private static final String LGBTQ_COMMANDS_DESC = "LGBTQ+ commands";
    private static String FUN_COMMANDS = "";
    private static final String FUN_COMMANDS_DESC = "Fun commands";
    private static String UTILS_COMMANDS = "";
    private static final String UTILS_COMMANDS_DESC = "Util commands";
    private static String MODERATION_COMMANDS = "";
    private static final String MODERATION_COMMANDS_DESC = "Mod commands";
    private static String ADMINISTRATION_COMMANDS = "";
    private static final String ADMINISTRATION_COMMANDS_DESC = "Admin commands";
    private static String PATRON_COMMANDS = "";
    private static final String PATRON_COMMANDS_DESC = "Patron only commands";
    private static String NSFW_COMMANDS = "";
    private static final String NSFW_COMMANDS_DESC = "NSFW commands";
    /// </editor-fold>

    private static final boolean INLINE = true;

    public static EmbedBuilder generateCommandEmbed(String prefix, CommandCategory... categories) {
        final EmbedBuilder embed = getDefaultEmbed()
            .setTitle("Click here for the support server", "https://dunctebot.link/server")
            .setDescription("Use `" + prefix + "help [command]` to get more info about a command\n");

        if (categories == null || categories.length == 0) {
            addAllCategoriesToEmbed(embed);
        } else {
            for (final CommandCategory category : categories) {
                final MessageEmbed.Field generated = getFieldForCategory(category);

                if (generated != null) {
                    embed.addField(generated);
                }
            }
        }

        return embed.addField("Support",
            "Support server: [https://dunctebot.link/server](https://dunctebot.link/server)\n" +
                "Support development of this bot: [https://www.patreon.com/DuncteBot](https://www.patreon.com/DuncteBot)", false);
    }

    /// <editor-fold desc="Reflection magic" defaultstate="collapsed">
    public static void init(final CommandManager manager) {
        final CommandCategory[] categories = CommandCategory.values();
        final Class<?> cls = HelpEmbeds.class;

        for (final CommandCategory category : categories) {

            if ("unlisted".equalsIgnoreCase(category.name())) {
                continue;
            }

            try {
                final List<String> cmds = new ArrayList<>();
                final String fieldName = category.name() + "_COMMANDS";
                final Field field = cls.getDeclaredField(fieldName);

                final List<ICommand> foundCommands = manager.getCommands(category);

                for (final ICommand command : foundCommands) {
                    cmds.add(command.getName());

                    if (command.shouldDisplayAliasesInHelp()) {
                        cmds.addAll(Arrays.asList(command.getAliases()));
                    }
                }

                field.set(cls, joinCommands(cmds));

            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private static void addAllCategoriesToEmbed(EmbedBuilder embed) {
        final CommandCategory[] categories = CommandCategory.values();
        final Class<?> cls = HelpEmbeds.class;

        for (final CommandCategory category : categories) {

            if ("unlisted".equalsIgnoreCase(category.name())) {
                continue;
            }

            try {
                final String fieldName = category.name() + "_COMMANDS";
                final String descFieldName = category.name() + "_COMMANDS_DESC";

                final Field field = cls.getDeclaredField(fieldName);
                final Field descField = cls.getDeclaredField(descFieldName);

                embed.addField(
                    (String) descField.get(cls),
                    (String) field.get(cls),
                    INLINE
                );
            }
            catch (NoSuchFieldException ignored) {
                // ignored
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    private static MessageEmbed.Field getFieldForCategory(CommandCategory category) {
        if ("unlisted".equalsIgnoreCase(category.name())) {
            return null;
        }

        final Class<?> cls = HelpEmbeds.class;
        final String fieldName = category.name() + "_COMMANDS";
        final String descFieldName = category.name() + "_COMMANDS_DESC";
        try {
            final Field field = cls.getDeclaredField(fieldName);
            final Field descField = cls.getDeclaredField(descFieldName);

            return new MessageEmbed.Field(
                (String) descField.get(cls),
                (String) field.get(cls),
                INLINE
            );
        }
        catch (NoSuchFieldException ignored) {
            return null;
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
    /// </editor-fold>

    private static String joinCommands(List<String> cmdNames) {
        return "`" + String.join("` | `", cmdNames) + "`";
    }
}
