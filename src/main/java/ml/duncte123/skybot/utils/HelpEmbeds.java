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

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.ICommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

import static me.duncte123.botcommons.messaging.EmbedUtils.defaultEmbed;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class HelpEmbeds {

    /**
     * These lists hold the commands for each category
     */
    private static List<String> mainCommands = new ArrayList<>();
    private static List<String> animalCommands = new ArrayList<>();
    private static List<String> funCommands = new ArrayList<>();
    private static List<String> musicCommands = new ArrayList<>();
    private static List<String> nerdCommands = new ArrayList<>();
    private static List<String> modAdminCommands = new ArrayList<>();
    private static List<String> patronCommands = new ArrayList<>();
    private static List<String> weebCommands = new ArrayList<>();
    private static List<String> NSFWCommands = new ArrayList<>();
    /**
     * This tells the fields to be inline or not
     */
    private static final boolean INLINE = true;

    /**
     * This loads all the commands in the lists
     */
    public static void init(CommandManager manager) {
        for (ICommand c : manager.getCommands()) {
            switch (c.getCategory()) {
                case MAIN:
                    mainCommands.add(c.getName());
                    break;
                case FUN:
                    funCommands.add(c.getName());
                    break;
                case ANIMALS:
                    animalCommands.add(c.getName());
                    break;
                case MUSIC:
                    musicCommands.add(c.getName());
                    break;
                case MOD_ADMIN:
                    modAdminCommands.add(c.getName());
                    break;
                case NERD_STUFF:
                    nerdCommands.add(c.getName());
                    break;
                case PATRON:
                    patronCommands.add(c.getName());
                    break;
                case WEEB:
                    weebCommands.add(c.getName());
                    break;
                case NSFW:
                    NSFWCommands.add(c.getName());
                    break;
            }

            if (c.shouldDisplayAliasesInHelp())
                for (String alias : c.getAliases()) {
                    switch (c.getCategory()) {
                        case MAIN:
                            mainCommands.add(alias);
                            break;
                        case FUN:
                            funCommands.add(alias);
                            break;
                        case ANIMALS:
                            animalCommands.add(alias);
                            break;
                        case MUSIC:
                            musicCommands.add(alias);
                            break;
                        case MOD_ADMIN:
                            modAdminCommands.add(alias);
                            break;
                        case NERD_STUFF:
                            nerdCommands.add(alias);
                            break;
                        case PATRON:
                            patronCommands.add(alias);
                            break;
                        case WEEB:
                            weebCommands.add(alias);
                            break;
                        case NSFW:
                            NSFWCommands.add(alias);
                            break;
                    }
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
    /*public static MessageEmbed generateCommandEmbed(String prefix) {
        return generateCommandEmbed(prefix, null);
    }*/
    public static MessageEmbed generateCommandEmbed(String prefix, CommandCategory... categories) {
        EmbedBuilder embed = defaultEmbed()
            .setThumbnail(Settings.DEFAULT_ICON)
            .setTitle("Click here for the support guild", "https://discord.gg/NKM9Xtk")
            .setDescription("Use `" + prefix + "help [command]` to get more info about a command");
        if (categories == null || categories.length == 0) {
            return embed
                .addField("Main commands", joinCommands(mainCommands), INLINE)
                .addField("Music commands", joinCommands(musicCommands), INLINE)
                .addField("Animal commands", joinCommands(animalCommands), INLINE)
                .addField("Weeb commands", joinCommands(weebCommands), INLINE)
                .addField("Fun commands", joinCommands(funCommands), INLINE)
                .addField("Nerd commands", joinCommands(nerdCommands), INLINE)
                .addField("Mod/Admin commands", joinCommands(modAdminCommands), INLINE)
                .addField("Patron only commands", joinCommands(patronCommands), INLINE)
                .addField("NSFW commands", joinCommands(NSFWCommands), INLINE)
                .addField("Other suff",
                    "Support server: [https://discord.gg/NKM9Xtk](https://discord.gg/NKM9Xtk)\n" +
                        "Support development of this bot: [https://www.patreon.com/DuncteBot](https://www.patreon.com/DuncteBot)", false)
                .build();
        }
        for (CommandCategory category : categories) {
            switch (category) {
                case FUN:
                    embed.addField("Fun commands", joinCommands(funCommands), INLINE);
                    break;
                case MAIN:
                    embed.addField("Main commands", joinCommands(mainCommands), INLINE);
                    break;
                case NSFW:
                    embed.addField("NSFW commands", joinCommands(NSFWCommands), INLINE);
                    break;
                case NERD_STUFF:
                    embed.addField("Nerd commands", joinCommands(nerdCommands), INLINE);
                    break;
                case WEEB:
                    embed.addField("Weeb commands", joinCommands(weebCommands), INLINE);
                    break;
                case MUSIC:
                    embed.addField("Music commands", joinCommands(musicCommands), INLINE);
                    break;
                case PATRON:
                    embed.addField("Patron only commands", joinCommands(patronCommands), INLINE);
                    break;
                case ANIMALS:
                    embed.addField("Animal commands", joinCommands(animalCommands), INLINE);
                    break;
                case MOD_ADMIN:
                    embed.addField("Mod/Admin commands", joinCommands(modAdminCommands), INLINE);
                    break;
                case UNLISTED:
                    break;
            }
        }

        return embed.addField("Other suff",
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
