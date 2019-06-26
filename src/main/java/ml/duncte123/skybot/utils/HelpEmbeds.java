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

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.ICommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.duncte123.botcommons.messaging.EmbedUtils.defaultEmbed;

@SuppressWarnings("FieldCanBeLocal")
@Author(nickname = "duncte123", author = "Duncan Sterken")
public class HelpEmbeds {

    /**
     * These hold the command lists
     */
    private static String MAIN_COMMANDS = "";
    private static String MUSIC_COMMANDS = "";
    private static String ANIMALS_COMMANDS = "";
    private static String WEEB_COMMANDS = "";
    private static String LGBTQ_COMMANDS = "";
    private static String FUN_COMMANDS = "";
    private static String UTIL_COMMANDS = "";
    private static String MODERATION_COMMANDS = "";
    private static String ADMINISTRATION_COMMANDS = "";
    private static String PATRON_COMMANDS = "";
    private static String NSFW_COMMANDS = "";

    /**
     * This tells the fields to be inline or not
     */
    private static final boolean INLINE = true;

    /**
     * This loads all the commands in the lists
     */

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

    /**
     * This will return a embed containing all the commands
     *
     * @param prefix
     *         the prefix that we need
     *
     * @return a embed containing all the commands
     */
    public static MessageEmbed generateCommandEmbed(String prefix, CommandCategory... categories) {
        final EmbedBuilder embed = defaultEmbed()
            .setThumbnail(Settings.DEFAULT_ICON)
            .setTitle("Click here for the support guild", "https://discord.gg/NKM9Xtk")
            .setDescription("Use `" + prefix + "help [command]` to get more info about a command\n")
            .appendDescription("`<Required Argument>` `[Optional Argument]`");

        if (categories == null || categories.length == 0) {
            return embed
                .addField("Main commands", MAIN_COMMANDS, INLINE)
                .addField("Music commands", MUSIC_COMMANDS, INLINE)
                .addField("Animal commands", ANIMALS_COMMANDS, INLINE)
                .addField("Weeb commands", WEEB_COMMANDS, INLINE)
                .addField("LGBTQ+ commands", LGBTQ_COMMANDS, INLINE)
                .addField("Fun commands", FUN_COMMANDS, INLINE)
                .addField("Util commands", UTIL_COMMANDS, INLINE)
                .addField("Mod commands", MODERATION_COMMANDS, INLINE)
                .addField("Admin commands", ADMINISTRATION_COMMANDS, INLINE)
                .addField("Patron only commands", PATRON_COMMANDS, INLINE)
                .addField("NSFW commands", NSFW_COMMANDS, INLINE)
                .addField("Other suff",
                    "Support server: [https://discord.gg/NKM9Xtk](https://discord.gg/NKM9Xtk)\n" +
                        "Support development of this bot: [https://www.patreon.com/DuncteBot](https://www.patreon.com/DuncteBot)", false)
                .build();
        }

        for (final CommandCategory category : categories) {
            switch (category) {
                case MAIN:
                    embed.addField("Main commands", MAIN_COMMANDS, INLINE);
                    break;
                case MUSIC:
                    embed.addField("Music commands", MUSIC_COMMANDS, INLINE);
                    break;
                case ANIMALS:
                    embed.addField("Animal commands", ANIMALS_COMMANDS, INLINE);
                    break;
                case WEEB:
                    embed.addField("Weeb commands", WEEB_COMMANDS, INLINE);
                    break;
                case LGBTQ:
                    embed.addField("LGBTQ+ commands", LGBTQ_COMMANDS, INLINE);
                    break;
                case FUN:
                    embed.addField("Fun commands", FUN_COMMANDS, INLINE);
                    break;
                case UTILS:
                    embed.addField("Util commands", UTIL_COMMANDS, INLINE);
                    break;
                case MODERATION:
                    embed.addField("Mod commands", MODERATION_COMMANDS, INLINE);
                    break;
                case ADMINISTRATION:
                    embed.addField("Admin commands", ADMINISTRATION_COMMANDS, INLINE);
                    break;
                case PATRON:
                    embed.addField("Patron only commands", PATRON_COMMANDS, INLINE);
                    break;
                case NSFW:
                    embed.addField("NSFW commands", NSFW_COMMANDS, INLINE);
                    break;
                default:
                    break;
            }
        }

        return embed.addField("Other stuff",
            "Support server: [https://discord.gg/NKM9Xtk](https://discord.gg/NKM9Xtk)\n" +
                "Support development of this bot: [https://www.patreon.com/DuncteBot](https://www.patreon.com/DuncteBot)", false)
            .build();

    }

    /**
     * if you enter a list of commands in here it will generate a string containing all the commands
     *
     * @param cmdNames
     *         the commands that should be added to the list
     *
     * @return a concatenated string of the commands that we entered
     */
    private static String joinCommands(List<String> cmdNames) {
        return "`" + String.join("`, `", cmdNames) + "`";
    }
}
