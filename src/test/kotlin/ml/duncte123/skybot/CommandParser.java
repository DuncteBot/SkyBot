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

package ml.duncte123.skybot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.ICommand;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Biggest cheat ever
 * We're using the test task to generate some files that we push to git
 */
public class CommandParser {

    @Test
    public void parseCommandToJson() throws Exception {
        final Collection<ICommand> commands = getCommands();
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode output = mapper.createArrayNode();

        for (final ICommand command : commands) {
            final ObjectNode obj = output.addObject();

            obj.put("name", command.getName())
                .put("help", parseHelp(command));

            final ArrayNode aliases = obj.putArray("aliases");

            Arrays.stream(command.getAliases()).forEach(aliases::add);
        }

        final File outputFile = new File("static_files/commands.json");

        if (!outputFile.exists()) outputFile.createNewFile();

        Files.write(outputFile.toPath(), mapper.writeValueAsBytes(output), StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Test
    public void parseCommandsToPHP() throws IOException {
        final Collection<ICommand> commands = getCommands();
        final StringBuilder builder = new StringBuilder();

        builder.append("<?php\n")
            .append("$a = [\n");

        for (ICommand command : commands) {
            final Class<? extends ICommand> aClass = command.getClass();
            final String path = aClass.getName().replaceAll("\\.", "/");
            final String simpleName = aClass.getSimpleName();
            final boolean kotlin = isKotlin(aClass);

            builder.append("\t[\n")
            .append("\t\t'name' => '").append(simpleName).append("',\n")
            .append("\t\t'path' => '").append(path).append("',\n")
            .append("\t\t'type' => '").append(kotlin ? "kotlin" : "java").append("',\n")
            .append("\t],\n");
        }

        builder.append("];\n");

        final File outputFile = new File("static_files/commands.php");

        if (!outputFile.exists()) outputFile.createNewFile();

        Files.write(outputFile.toPath(), builder.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Test
    public void parseCommandsToJekyll() throws IOException {
        final List<String> names = getCommands()
            .stream()
            .filter((cmd) -> cmd.getCategory() != CommandCategory.UNLISTED)
            .map(ICommand::getName)
            .sorted()
            .collect(Collectors.toList());
        final CommandManager commandManager = Variables.getInstance().getCommandManager();
        final StringBuilder builder = new StringBuilder();

        builder.append("---\n")
            .append("layout: default\n")
            .append("commands:\n");

        names.forEach((it) -> {
            final ICommand command = commandManager.getCommand(it);
            final String desc = parseHelp(command).replaceAll("\"", "\\\"");

            builder.append("  - name: ").append(it)
                .append("\n    description: \"").append(desc).append("\"\n");
        });

        builder.append("---\n\n{{ content }}\n");


        final File outputFile = new File("static_files/command_storage.html");

        if (!outputFile.exists()) outputFile.createNewFile();

        Files.write(outputFile.toPath(), builder.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String parseHelp(ICommand cmd) {
        final StringBuilder builder = new StringBuilder();

        builder.append(
            cmd.help()
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\\n", "<br />")
                .replaceAll("\\`\\`\\`(.*)\\`\\`\\`", "<pre class=\"code-block\"><code>$1</code></pre>")
                .replaceAll("\\`([^\\`]+)\\`", "<code>$1</code>")
                .replaceAll("\\*\\*(.*)\\*\\*", "<strong>$1</strong>")
        );

        if (cmd.getAliases().length > 0 && cmd.shouldDisplayAliasesInHelp()) {
            builder.append("<br />Aliases: ")
                .append(Settings.PREFIX)
                .append(String.join(", " + Settings.PREFIX, cmd.getAliases()));
        }


        return builder.toString();
    }

    private boolean isKotlin(Class<? extends ICommand> clazz) {
        final Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();

        if (declaredAnnotations.length > 0) {
            return Arrays.stream(declaredAnnotations)
                .map(Annotation::annotationType)
                .anyMatch((it) -> it.getName().equals("kotlin.Metadata"));
        }

        return false;
    }

    private Collection<ICommand> getCommands() {
        return Variables.getInstance().getCommandManager().getCommands();
    }
}
