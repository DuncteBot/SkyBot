/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

import ml.duncte123.skybot.objects.command.Command;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HelpEmbeds {

    /**
     * This tells the fields to be inline or not
     */
    private static boolean INLINE = false;

    /**
     * These lists hold the commands for each category
     */
    private static List<String> mainCommands = new ArrayList<>();
    private static List<String> animalCommands = new ArrayList<>();
    private static List<String> funCommands = new ArrayList<>();
    private static List<String> musicCommands = new ArrayList<>();
    private static List<String> nerdCommands = new ArrayList<>();
    private static List<String> modAdminCommands = new ArrayList<>();

    /**
     * This loads all the commands in the lists
     */
    public static void init() {
        for(Command c : AirUtils.commandManager.getCommands()) {
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
            }

            for(String alias : c.getAliases()) {
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
                }
            }
        }
    }

    /**
     * This is the embed containing all the commands
     */
    public static MessageEmbed commandList = getCommandList();

    /**
     * This will return a embed containing all the commands
     * @return a embed containing all the commands
     */
    public static MessageEmbed getCommandList() {
        return getCommandListWithPrefix(Settings.prefix);
    }

    /**
     * This will return a embed containing all the commands
     * @param prefix the prefix that we need
     * @return a embed containing all the commands
     */
    public static MessageEmbed getCommandListWithPrefix(String prefix) {
        return EmbedUtils.defaultEmbed()
                .setDescription("Use `"+ prefix+"help [command]` to get more info about a command")
                .addField("Main commands", generateCommandsWithPrefix(prefix, mainCommands), INLINE)
                .addField("Animal commands", generateCommandsWithPrefix(prefix, animalCommands), INLINE)
                .addField("Music commands", generateCommandsWithPrefix(prefix, musicCommands), INLINE)
                .addField("Fun commands", generateCommandsWithPrefix(prefix, funCommands), INLINE)
                .addField("Nerd commands", generateCommandsWithPrefix(prefix, nerdCommands), INLINE)
                .addField("Mod/Admin commands", generateCommandsWithPrefix(prefix, modAdminCommands), INLINE)
                .build();
    }

    /**
     * if you enter a list of commands in here it will generate a string containing all the commands
     * @param prefix The prefix that will be in frond of the commands
     * @param cmdNames the commands that should be added to the list
     * @return a concatenated string of the commands that we entered
     */
    public static String generateCommandsWithPrefix(String prefix, String... cmdNames) {
        StringBuilder out = new StringBuilder();

        for (String name : cmdNames) {
            out.append("`").append(prefix).append(name).append("` ");
        }

        return out.toString();
    }

    /**
     * if you enter a list of commands in here it will generate a string containing all the commands
     * @param prefix The prefix that will be in frond of the commands
     * @param cmdNames the commands that should be added to the list
     * @return a concatenated string of the commands that we entered
     */
    public static String generateCommandsWithPrefix(String prefix, List cmdNames) {
        String[] cmdArray = (String[]) cmdNames.toArray(new String[0]);
        return generateCommandsWithPrefix(prefix, cmdArray);
    }

    /**
     * if you enter a list of commands in here it will generate a string containing all the commands
     * @param cmdNames the commands that should be added to the list
     * @return a concatenated string of the commands that we entered
     */
    public static String generateCommands(String... cmdNames) {
        return generateCommandsWithPrefix(Settings.prefix, cmdNames);
    }
}
