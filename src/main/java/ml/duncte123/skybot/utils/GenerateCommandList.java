/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
import ml.duncte123.skybot.objects.command.CommandCategory;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a simple util class that generates a list of commands in a file for me
 *
 * @author duncte123
 */
public class GenerateCommandList {

    public static void inPHP() throws Exception{
        File phpFile = new File("commandList.php");

        if(!phpFile.exists())
            phpFile.createNewFile();
        else {
            phpFile.delete();
            phpFile.createNewFile();
        }

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(phpFile), "UTF-8"));
        writer.write("<?php");
        writer.newLine();
        writer.append("$commands = [");
        writer.newLine();

        List<String> names = new ArrayList<>();
        AirUtils.commandManager.getCommands().parallelStream().forEach(c-> names.add(c.getName()));
        Collections.sort(names);

        for (String n: names) {
            Command cmd = AirUtils.commandManager.getCommand(n);
            if(!cmd.getCategory().equals(CommandCategory.UNLISTED)) {
                writer.append("\t")
                        .append('"')
                        .append(cmd.getName())
                        .append('"')
                        .append(" => ")
                        .append('"')
                        .append(cmd.help()
                                .replaceAll("`(.*)`", "<code>$1</code>")
                                .replaceAll("\\n", "<br />")
                                .replaceAll("\\*\\*(.*)\\*\\*", "<strong>$1</strong>")
                        );
                if (cmd.getAliases().length > 0) {
                    writer.append("<br />")
                            .append("Aliases: ")
                            .append(StringUtils.join(cmd.getAliases(), ", "));
                }
                writer.append("\",");
                writer.newLine();
            }
        }

        writer.newLine();
        writer.append("];");
        writer.close();
    }

}
