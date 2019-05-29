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

import io.sentry.Sentry;
import kotlin.Triple;
import ml.duncte123.skybot.exceptions.DoomedException;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.utils.CustomCommandUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.reflections.Reflections;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.unstable.utils.ComparatingUtils.execCheck;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class CommandManager {

    private static final Pattern COMMAND_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
    private final ExecutorService commandThread = Executors.newCachedThreadPool((t) -> new Thread(t, "Command-execute-thread"));
    /**
     * This stores all our commands
     */
    private final Map<String, ICommand> commands = new ConcurrentHashMap<>();
    private final Map<String, String> aliases = new ConcurrentHashMap<>();

    private final Set<CustomCommand> customCommands = ConcurrentHashMap.newKeySet();

    private final Variables variables;

    /**
     * This makes sure that all the commands are added
     */
    public CommandManager(Variables variables) {
        this.variables = variables;

        //Get reflections for this project
        registerCommandsFromReflection(new Reflections("ml.duncte123.skybot.commands"));
//        registerCommandsFromReflection(new Reflections("ml.duncte123.skybot.unstable.commands"));

        loadCustomCommands();
    }

    /**
     * This is method to get the commands on request
     *
     * @return A list of all the commands
     */
    public Collection<ICommand> getCommands() {
        return this.commands.values();
    }

    Map<String, ICommand> getCommandsMap() {
        return this.commands;
    }

    Map<String, String> getAliasesMap() {
        return aliases;
    }

    public Set<CustomCommand> getCustomCommands() {
        return this.customCommands;
    }

    /**
     * This tries to get a command with the provided name/alias
     *
     * @param name
     *         the name of the command
     *
     * @return a possible null command for the name
     */
    public ICommand getCommand(String name) {

        ICommand found = this.commands.get(name);

        if (found == null) {
            final String forAlias = this.aliases.get(name);

            if (forAlias != null) {
                found = this.commands.get(forAlias);
            }
        }

        return found;
    }

    public List<ICommand> getCommands(CommandCategory category) {
        return this.commands.values().stream().filter(c -> c.getCategory().equals(category)).collect(Collectors.toList());
    }


    public CustomCommand getCustomCommand(String invoke, long guildId) {
        return this.customCommands.stream().filter((c) -> c.getGuildId() == guildId)
            .filter((c) -> c.getName().equalsIgnoreCase(invoke)).findFirst().orElse(null);
    }

    public List<CustomCommand> getCustomCommands(long guildId) {
        return this.customCommands.stream().filter((c) -> c.getGuildId() == guildId).collect(Collectors.toList());
    }

    public List<CustomCommand> getAutoResponses(long guildId) {
        return this.customCommands.stream()
            .filter((c) -> c.getGuildId() == guildId)
            .filter(CustomCommand::isAutoResponse)
            .collect(Collectors.toList());
    }

    public boolean editCustomCommand(CustomCommand c) {
        return addCustomCommand(c, true, true).getFirst();
    }

    public Triple<Boolean, Boolean, Boolean> addCustomCommand(CustomCommand c) {
        return addCustomCommand(c, true, false);
    }

    private Triple<Boolean, Boolean, Boolean> addCustomCommand(CustomCommand command, boolean insertInDb, boolean isEdit) {
        if (command.getName().contains(" ")) {
            throw new DoomedException("Name can't have spaces!");
        }

        final boolean commandFound = this.customCommands.stream()
            .anyMatch((cmd) -> cmd.getName().equalsIgnoreCase(command.getName()) && cmd.getGuildId() == command.getGuildId()) && !isEdit;
        final boolean limitReached = this.customCommands.stream().filter((cmd) -> cmd.getGuildId() == command.getGuildId()).count() >= 50 && !isEdit;

        if (commandFound || limitReached) {
            return new Triple<>(false, commandFound, limitReached);
        }

        if (insertInDb) {
            try {
                final CompletableFuture<Triple<Boolean, Boolean, Boolean>> future = new CompletableFuture<>();

                if (isEdit) {
                    this.variables.getDatabaseAdapter()
                        .updateCustomCommand(command.getGuildId(), command.getName(), command.getMessage(), command.isAutoResponse(), (triple) -> {
                            future.complete(triple);
                            return null;
                        });
                } else {
                    this.variables.getDatabaseAdapter()
                        .createCustomCommand(command.getGuildId(), command.getName(), command.getMessage(), (triple) -> {
                            future.complete(triple);
                            return null;
                        });
                }

                final Triple<Boolean, Boolean, Boolean> res = future.get();

                if (res != null && !res.getFirst()) {
                    return res;
                }
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Sentry.capture(e);
            }
        }

        if (isEdit) {
            this.customCommands.remove(getCustomCommand(command.getName(), command.getGuildId()));
        }

        this.customCommands.add(command);

        return new Triple<>(true, false, false);
    }

    /*
     * This removes a command from the commands
     *
     * @param command the command to remove
     * @return {@code true} on success
     */
    /*public boolean removeCommand(String command) {
        return commands.remove(getCommand(command));
    }*/

    public boolean removeCustomCommand(String name, long guildId) {
        final CustomCommand cmd = getCustomCommand(name, guildId);

        if (cmd == null) {
            return false;
        }

        try {
            final CompletableFuture<Boolean> future = new CompletableFuture<>();
            this.variables.getDatabaseAdapter().deleteCustomCommand(guildId, name, future::complete);

            final boolean result = future.get();

            if (result) {
                this.customCommands.remove(cmd);
            }

            return result;
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This handles adding the command
     *
     * @param command
     *         The command to add
     *
     * @throws IllegalArgumentException
     *         if the command or alias is already present
     */
    private void addCommand(ICommand command) {
        if (command.getName().contains(" ")) {
            throw new DoomedException("Name can't have spaces!");
        }

        final String cmdName = command.getName().toLowerCase();

        if (this.commands.containsKey(cmdName)) {
            throw new IllegalArgumentException(String.format("Command %s already present", cmdName));
        }

        final List<String> lowerAliasses = Arrays.stream(command.getAliases()).map(String::toLowerCase).collect(Collectors.toList());

        if (!lowerAliasses.isEmpty()) {
            for (final String alias : lowerAliasses) {
                if (this.aliases.containsKey(alias)) {
                    throw new IllegalArgumentException(String.format(
                        "Alias %s already present (Stored for: %s, trying to insert: %s))",
                        alias,
                        this.aliases.get(alias),
                        command.getName()
                    ));
                }
            }

            for (final String alias : lowerAliasses) {
                this.aliases.put(alias, command.getName());
            }
        }

        this.commands.put(cmdName, command);
    }

    /**
     * This will run the command when we need them
     *
     * @param event
     *         the event for the message
     */
    public void runCommand(GuildMessageReceivedEvent event) {
        final String customPrefix = GuildSettingsUtils.getGuild(event.getGuild(), variables).getCustomPrefix();
        final String[] split = event.getMessage().getContentRaw().replaceFirst(
            "(?i)" + Pattern.quote(Settings.PREFIX) + "|" + Pattern.quote(Settings.OTHER_PREFIX) + "|" +
                Pattern.quote(customPrefix),
            "").split("\\s+", 2);
        final String invoke = split[0].toLowerCase();

        final List<String> args = new ArrayList<>();

        if (split.length > 1) {
            final String raw = split[1];
            final Matcher m = COMMAND_PATTERN.matcher(raw);
            while (m.find()) {
                args.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
            }
        }

        dispatchCommand(invoke, args, event);
    }

    private void dispatchCommand(String invoke, List<String> args, GuildMessageReceivedEvent event) {
        ICommand cmd = getCommand(invoke);

        if (cmd == null) {
            cmd = getCustomCommand(invoke, event.getGuild().getIdLong());
        }

        dispatchCommand(cmd, invoke, args, event);
    }

    public void dispatchCommand(ICommand cmd, String invoke, List<String> args, GuildMessageReceivedEvent event) {

        if (cmd == null) {
            return;
        }

        this.commandThread.submit(() -> {

            MDC.put("command.invoke", invoke);
            MDC.put("command.args", args.toString());
            MDC.put("user.tag", event.getAuthor().getAsTag());
            MDC.put("user.id", event.getAuthor().getId());
            MDC.put("guild", event.getGuild().toString());

            final TextChannel channel = event.getChannel();

            if (!channel.canTalk()) {
                return;
            }

            // Suppress errors from when we can't type in the channel
            channel.sendTyping().queue(null, (t) -> {});

            try {

                if (!cmd.isCustom()) {

                    if (cmd.getCategory() == CommandCategory.NSFW && !channel.isNSFW()) {
                        sendMsg(event, "Woops, this channel is not marked as NSFW.\n" +
                            "Please mark this channel as NSFW to use this command");
                        return;
                    }

                    MDC.put("command.class", cmd.getClass().getName());

                    cmd.executeCommand(
                        new CommandContext(invoke, args, event)
                    );

                    return;
                }

                final CustomCommand cc = (CustomCommand) cmd;

                if (cc.getGuildId() != event.getGuild().getIdLong()) {
                    return;
                }

                try {
                    MDC.put("command.custom.message", cc.getMessage());

                    final String message = CustomCommandUtils.parse(new CommandContext(invoke, args, event), cc.getMessage());

                    if (!message.isEmpty()) {
                        sendMsg(event, "\u200B" + message);
                    }
                }
                catch (Exception e) {
                    sendMsg(event, "Error with parsing custom command: " + e.getMessage());
                    execCheck(e);
                }

            }
            catch (Throwable ex) {
                execCheck(ex);
                ex.printStackTrace();
            }
        });
    }

    private void registerCommandsFromReflection(Reflections reflections) {
        //Loop over them commands
        for (final Class<? extends ICommand> cmd : reflections.getSubTypesOf(ICommand.class)) {
            try {
                final ICommand command = cmd.getDeclaredConstructor().newInstance();
//                System.out.println(command.getName());
                //Add the command
                this.addCommand(command);
            }
            catch (Exception ignored) {
            }
        }
    }

    private void loadCustomCommands() {
        this.variables.getDatabaseAdapter().getCustomCommands(
            (loadedCommands) -> {
                loadedCommands.forEach(
                    (command) -> addCustomCommand(command, false, false)
                );

                return null;
            }
        );
    }

    public void shutdown() {
        this.commandThread.shutdown();
    }
}
