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

import com.jagrosh.jagtag.Parser;
import kotlin.Triple;
import ml.duncte123.skybot.exceptions.DoomedException;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.utils.CustomCommandUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.reflections.Reflections;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.unstable.utils.ComparatingUtils.execCheck;

@SuppressWarnings("WeakerAccess")
@Author(nickname = "duncte123", author = "Duncan Sterken")
public class CommandManager {

    public final ExecutorService commandThread = Executors.newCachedThreadPool(t -> new Thread(t, "Command-execute-thread"));
    private static final Pattern COMMAND_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
    /**
     * This stores all our commands
     */
    private final Set<ICommand> commands = ConcurrentHashMap.newKeySet();
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
    public Set<ICommand> getCommands() {
        return commands;
    }

    public Set<CustomCommand> getCustomCommands() {
        return customCommands;
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
        Optional<ICommand> cmd = commands.stream().filter((c) -> c.getName().equals(name)).findFirst();

        if (!cmd.isPresent()) {
            cmd = commands.stream().filter((c) -> Arrays.asList(c.getAliases()).contains(name)).findFirst();
        }

        return cmd.orElse(null);
    }

    public List<ICommand> getCommands(CommandCategory category) {
        return commands.stream().filter(c -> c.getCategory().equals(category)).collect(Collectors.toList());
    }


    public CustomCommand getCustomCommand(String invoke, long guildId) {
        return customCommands.stream().filter((c) -> c.getGuildId() == guildId)
            .filter((c) -> c.getName().equalsIgnoreCase(invoke)).findFirst().orElse(null);
    }

    public List<CustomCommand> getCustomCommands(long guildId) {
        return customCommands.stream().filter((c) -> c.getGuildId() == guildId).collect(Collectors.toList());
    }

    public List<CustomCommand> getAutoResponses(long guildId) {
        return customCommands.stream()
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

    public Triple<Boolean, Boolean, Boolean> addCustomCommand(CustomCommand command, boolean insertInDb, boolean isEdit) {
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
                    variables.getDatabaseAdapter()
                        .updateCustomCommand(command.getGuildId(), command.getName(), command.getMessage(), command.isAutoResponse(), (triple) -> {
                            future.complete(triple);
                            return null;
                        });
                } else {
                    variables.getDatabaseAdapter()
                        .createCustomCommand(command.getGuildId(), command.getName(), command.getMessage(), (triple) -> {
                            future.complete(triple);
                            return null;
                        });
                }

                final Triple<Boolean, Boolean, Boolean> res = future.get();

                if (res != null && !res.getFirst()) {
                    return res;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
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
            variables.getDatabaseAdapter().deleteCustomCommand(guildId, name, (bool) -> {
                future.complete(bool);
                return null;
            });

            final boolean result = future.get();

            if (result) {
                this.customCommands.remove(cmd);
            }

            return result;
        } catch (InterruptedException | ExecutionException e) {
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
     * @return true if the command is added
     */
    @SuppressWarnings({"UnusedReturnValue"})
    public boolean addCommand(ICommand command) {
        if (command.getName().contains(" ")) {
            throw new DoomedException("Name can't have spaces!");
        }

        if (this.commands.stream().anyMatch((cmd) -> cmd.getName().equalsIgnoreCase(command.getName()))) {
            @SinceSkybot(version = "3.52.1") final List<String> aliases = Arrays.asList(this.commands.stream().filter((cmd) -> cmd.getName()
                .equalsIgnoreCase(command.getName())).findFirst().get().getAliases());
            for (final String alias : command.getAliases()) {
                if (aliases.contains(alias)) {
                    return false;
                }
            }
            return false;
        }
        this.commands.add(command);

        return true;
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

    public void dispatchCommand(String invoke, List<String> args, GuildMessageReceivedEvent event) {
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

        commandThread.submit(() -> {

            event.getChannel().sendTyping().queue();

            try {

                if (!cmd.isCustom()) {

                    if (cmd.getCategory() == CommandCategory.NSFW && !event.getChannel().isNSFW()) {
                        sendMsg(event, "Woops, this channel is not marked as NSFW.\n" +
                            "Please mark this channel as NSFW to use this command");
                        return;
                    }

                    cmd.executeCommand(
                        new CommandContext(invoke, args, event, variables)
                    );

                    return;
                }

                final CustomCommand cc = (CustomCommand) cmd;

                if (cc.getGuildId() != event.getGuild().getIdLong()) {
                    return;
                }

                try {
                    final Parser parser = CustomCommandUtils.PARSER;

                    final String message = parser.clear()
                        .put("user", event.getAuthor())
                        .put("channel", event.getChannel())
                        .put("guild", event.getGuild())
                        .put("args", String.join(" ", args))
                        .parse(cc.getMessage());

                    /*JSONObject embedJson = parser.get("embed");

                    JDAImpl jda = (JDAImpl) event.getJDA();
                    MessageEmbed embed = jda.getEntityBuilder().createMessageEmbed(embedJson);*/

                    if (!message.isEmpty()) {
                        sendMsg(event, "\u200B" + message);
                    }

//                    sendEmbedRaw(event.getChannel(), embed, null);
                    parser.clear();
                } catch (Exception e) {
                    sendMsg(event, "Error with parsing custom command: " + e.getMessage());
                    execCheck(e);
                }

            } catch (Throwable ex) {
                execCheck(ex);
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
            } catch (Exception ignored) {
            }
        }
    }

    private void loadCustomCommands() {

        variables.getDatabaseAdapter().getCustomCommands(
            (loadedCommands) -> {
                loadedCommands.forEach(
                    (command) -> addCustomCommand(command, false, false)
                );

                return null;
            }
        );
    }

    public void shutdown() {
        commandThread.shutdown();
    }
}
