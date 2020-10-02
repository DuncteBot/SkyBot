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

package ml.duncte123.skybot;

import com.jagrosh.jagtag.Parser;
import gnu.trove.map.TObjectLongMap;
import io.sentry.Sentry;
import kotlin.Triple;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.commands.admin.BlackListCommand;
import ml.duncte123.skybot.commands.admin.VcAutoRoleCommand;
import ml.duncte123.skybot.commands.animals.*;
import ml.duncte123.skybot.commands.essentials.*;
import ml.duncte123.skybot.commands.essentials.eval.EvalCommand;
import ml.duncte123.skybot.commands.fun.*;
import ml.duncte123.skybot.commands.guild.GuildInfoCommand;
import ml.duncte123.skybot.commands.guild.GuildJoinsCommand;
import ml.duncte123.skybot.commands.guild.mod.*;
import ml.duncte123.skybot.commands.guild.owner.CustomCommandCommand;
import ml.duncte123.skybot.commands.guild.owner.ForceDisconnectCommand;
import ml.duncte123.skybot.commands.guild.owner.LockEmoteCommand;
import ml.duncte123.skybot.commands.guild.owner.UnlockEmoteCommand;
import ml.duncte123.skybot.commands.guild.owner.settings.*;
import ml.duncte123.skybot.commands.image.*;
import ml.duncte123.skybot.commands.image.duncte123gen.DrakeCommand;
import ml.duncte123.skybot.commands.image.duncte123gen.IWantToDieCommand;
import ml.duncte123.skybot.commands.image.duncte123gen.ItsFreeRealEstateCommand;
import ml.duncte123.skybot.commands.image.filter.*;
import ml.duncte123.skybot.commands.lgbtq.FlagCommand;
import ml.duncte123.skybot.commands.lgbtq.PronounsCheckCommand;
import ml.duncte123.skybot.commands.lgbtq.SetPronounsCommand;
import ml.duncte123.skybot.commands.mod.*;
import ml.duncte123.skybot.commands.music.*;
import ml.duncte123.skybot.commands.nsfw.CarsAndHentaiCommand;
import ml.duncte123.skybot.commands.nsfw.HentaiCommand;
import ml.duncte123.skybot.commands.nsfw.LewdKitsuneCommand;
import ml.duncte123.skybot.commands.nsfw.LewdNekoCommand;
import ml.duncte123.skybot.commands.patreon.ScreenshotCommand;
import ml.duncte123.skybot.commands.uncategorized.*;
import ml.duncte123.skybot.commands.utils.EmoteCommand;
import ml.duncte123.skybot.commands.weeb.*;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.objects.pairs.LongLongPair;
import ml.duncte123.skybot.utils.CommandUtils;
import ml.duncte123.skybot.utils.MapUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.AirUtils.setJDAContext;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class CommandManager {
    private static final TObjectLongMap<String> cooldowns = MapUtils.newObjectLongMap();
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);
    private static final Pattern COMMAND_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
    private static final ScheduledExecutorService cooldownThread = Executors.newSingleThreadScheduledExecutor((r) -> {
        final Thread thread = new Thread(r, "Command-cooldown-thread");
        thread.setDaemon(true);
        return thread;
    });
    private final ExecutorService commandThread = Executors.newCachedThreadPool((r) -> {
        final Thread thread = new Thread(r, "Command-execute-thread");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<String, ICommand> commands = new ConcurrentHashMap<>();
    private final Map<String, String> aliases = new ConcurrentHashMap<>();
    private final Set<CustomCommand> customCommands = ConcurrentHashMap.newKeySet();
    private final Variables variables;

    static {
        cooldownThread.scheduleWithFixedDelay(() -> {
                try {
                    // Loop over all cooldowns with a 5 minute interval
                    // This makes sure that we don't have any useless cooldowns in the system hogging up memory
                    cooldowns.forEachEntry((key, val) -> {
                        final long remaining = calcTimeRemaining(val);

                        // Remove the value from the cooldowns if it is less or equal to 0
                        if (remaining <= 0) {
                            cooldowns.remove(key);
                        }

                        // Return true to indicate that we are allowed to continue the loop
                        return true;
                    });
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }, 5, 5, TimeUnit.MINUTES);
    }

    public CommandManager(Variables variables) {
        this.variables = variables;
        Reflections reflections=new Reflections("ml.duncte123.skybot.commands",new SubTypesScanner());//scan commands package for commands
        Map<Class<?>,Object> possibleParams=new HashMap<>();//parameters that could be passed to the constructor, detected by type
        possibleParams.put(Variables.class,variables);
        for (Class<? extends Command> commandClass : reflections.getSubTypesOf(Command.class)) {//iterate over all subclasses of Command in that package
            try {
                if (!Modifier.isAbstract(commandClass.getModifiers())) {//skip if abstract
                    Constructor<?>[] constructors = commandClass.getConstructors();
                    Command instance=null;
                    for (int i = 0; instance==null&&i < constructors.length; i++) {//iterate through constructors until a matching one is found
                        Class<?>[] parameterTypes = constructors[i].getParameterTypes();
                        Object[] params=new Object[parameterTypes.length];
                        for (int j = 0; params!=null&&j < parameterTypes.length; j++) {//iterate over constructor parameters
                            Class<?> param=parameterTypes[j];
                            if(possibleParams.containsKey(param)){//constructor parameter can be passed
                                params[j]=possibleParams.get(param);//add to arguments
                            }else{//constructor cannot be passed
                                params=null;//go on with the next constructor
                            }
                        }
                        if(params!=null){//valid constructor found
                            instance=(Command)constructors[i].newInstance(params);//create instance using this constructor
                        }
                    }
                    if(instance==null){//command not instantiated
                        if(LOGGER.isWarnEnabled()){
                            LOGGER.warn("No matching constructor found for command class {}.",commandClass.getSimpleName());
                        }
                    }else{
                        this.addCommand(instance);
                    }
                }
            }
            catch (InstantiationException|IllegalAccessException|InvocationTargetException e) {
                e.printStackTrace();
                if (LOGGER.isErrorEnabled()){
                    LOGGER.error("An exception occurred trying to create and register an instance of the command {}.",commandClass.getSimpleName(), e);
                }
            }
        }
        loadCustomCommands();
    }

    public Collection<ICommand> getCommands() {
        return this.commands.values();
    }

    LongLongPair getCommandCount() {
        return new LongLongPair(this.commands.size(), this.aliases.size());
    }

    public Set<CustomCommand> getCustomCommands() {
        return this.customCommands;
    }

    @Nullable
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

    public boolean isCommand(String customPrefix, String message) {
        final String[] split = message.toLowerCase().replaceFirst(
            "(?i)" + Pattern.quote(Settings.PREFIX) + '|' + Pattern.quote(Settings.OTHER_PREFIX) + '|' +
                Pattern.quote(customPrefix),
            "").split("\\s+", 2);

        if (split.length >= 1) {
            final String invoke = split[0].toLowerCase();

            return getCommand(invoke) != null;
        }


        return false;
    }

    public List<ICommand> getCommands(CommandCategory category) {
        return this.commands.values().stream().filter(c -> c.getCategory().equals(category)).collect(Collectors.toList());
    }

    @Nullable
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

    public void runCommand(GuildMessageReceivedEvent event, String customPrefix) {
        final String[] split = event.getMessage().getContentRaw().replaceFirst(
            "(?i)" + Pattern.quote(Settings.PREFIX) + '|' + Pattern.quote(Settings.OTHER_PREFIX) + '|' +
                Pattern.quote(customPrefix),
            "")
            .trim()
            .split("\\s+", 2);
        final String invoke = split[0];

        final List<String> args = new ArrayList<>();

        if (split.length > 1) {
            final String raw = split[1];
            final Matcher m = COMMAND_PATTERN.matcher(raw);
            while (m.find()) {
                args.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
            }
        }

        dispatchCommand(invoke, invoke.toLowerCase(), args, event);
    }

    public void setCooldown(String key, int seconds) {
        cooldowns.put(key, OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(seconds).toEpochSecond());
    }

    public long getRemainingCooldown(String key) {
        // If we don't have a cooldown for the command return 0
        if (!cooldowns.containsKey(key)) {
            return 0;
        }

        // get the time that the cooldown started
        final long startTime = cooldowns.get(key);
        // The time that is left until the cooldown is over
        final long timeLeft = calcTimeRemaining(startTime);

        // If the time is up we will return 0 and remove the keys from the cooldowns map
        if (timeLeft <= 0) {
            cooldowns.remove(key);
            return 0;
        }

        return timeLeft;
    }

    private static long calcTimeRemaining(long startTime) {
        // Get the start time as an OffsetDateTime
        final OffsetDateTime startTimeOffset = Instant.ofEpochSecond(startTime).atOffset(ZoneOffset.UTC);
        // get the time that is left for the cooldown
        return OffsetDateTime.now(ZoneOffset.UTC).until(startTimeOffset, ChronoUnit.SECONDS);
    }

    private void dispatchCommand(String invoke, String invokeLower, List<String> args, GuildMessageReceivedEvent event) {
        ICommand cmd = getCommand(invokeLower);

        if (cmd == null) {
            cmd = getCustomCommand(invokeLower, event.getGuild().getIdLong());
        }

        if (cmd == null) {
            return;
        }

        dispatchCommand(cmd, invoke, args, event);
    }

    public void dispatchCommand(@Nonnull ICommand cmd, String invoke, List<String> args, GuildMessageReceivedEvent event) {
        this.commandThread.submit(() -> {
            MDC.put("command.invoke", invoke);
            MDC.put("command.args", args.toString());
            MDC.put("user.tag", event.getAuthor().getAsTag());
            MDC.put("user.id", event.getAuthor().getId());
            MDC.put("guild", event.getGuild().toString());
            setJDAContext(event.getJDA());

            final TextChannel channel = event.getChannel();

            if (!channel.canTalk()) {
                return;
            }

            // Suppress errors from when we can't type in the channel
            channel.sendTyping().queue(null, (t) -> {});

            try {
                if (!cmd.isCustom()) {
                    runNormalCommand(cmd, invoke, args, event);
                } else {
                    runCustomCommand(cmd, invoke, args, event);
                }
            }
            catch (Throwable ex) {
                Sentry.capture(ex);
                ex.printStackTrace();
                sendMsg(MessageConfig.Builder.fromEvent(event)
                    .setMessage("Something went wrong whilst executing the command, my developers have been informed of this\n" + ex.getMessage())
                    .build());
            }
        });
    }

    private void runNormalCommand(ICommand cmd, String invoke, List<String> args, GuildMessageReceivedEvent event) {
        if (cmd.getCategory() == CommandCategory.NSFW && !event.getChannel().isNSFW()) {
            sendMsg(MessageConfig.Builder.fromEvent(event)
                .setMessage("Woops, this channel is not marked as NSFW.\n" +
                    "Please mark this channel as NSFW to use this command")
                .build());
            return;
        }

        MDC.put("command.class", cmd.getClass().getName());

        LOGGER.info("Dispatching command \"{}\" in guild \"{}\" with {}", cmd.getClass().getSimpleName(), event.getGuild(), args);

        cmd.executeCommand(
            new CommandContext(invoke, args, event, variables)
        );
    }

    private void runCustomCommand(ICommand cmd, String invoke, List<String> args, GuildMessageReceivedEvent event) {
        final CustomCommand cc = (CustomCommand) cmd;

        if (cc.getGuildId() != event.getGuild().getIdLong()) {
            return;
        }

        try {
            MDC.put("command.custom.message", cc.getMessage());

            final Parser parser = CommandUtils.getParser(new CommandContext(invoke, args, event, variables));

            final String message = parser.parse(cc.getMessage());
            final MessageConfig.Builder messageBuilder = MessageConfig.Builder.fromEvent(event);
            final DataObject object = parser.get("embed");
            boolean hasContent = false;

            if (!message.isEmpty()) {
                messageBuilder.setMessage("\u200B" + message);
                hasContent = true;
            }

            if (object != null) {
                final JDAImpl jda = (JDAImpl) event.getJDA();
                final EmbedBuilder embed = new EmbedBuilder(jda.getEntityBuilder().createMessageEmbed(object));

                messageBuilder.setEmbed(embed, true);
                hasContent = true;
            }

            if (hasContent) {
                sendMsg(messageBuilder.build());
            }


            parser.clear();
        }
        catch (Exception e) {
            sendMsg(MessageConfig.Builder.fromEvent(event)
                .setMessage("Error with parsing custom command: " + e.getMessage())
                .build());
            Sentry.capture(e);
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

    public boolean editCustomCommand(CustomCommand c) {
        return addCustomCommand(c, true, true).getFirst();
    }

    public Triple<Boolean, Boolean, Boolean> registerCustomCommand(CustomCommand c) {
        return addCustomCommand(c, true, false);
    }

    private Triple<Boolean, Boolean, Boolean> addCustomCommand(CustomCommand command, boolean insertInDb, boolean isEdit) {
        if (command.getName().contains(" ")) {
            throw new IllegalArgumentException("Name can't have spaces!");
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

    private void addCommand(ICommand command) {
        if (command.getName().contains(" ")) {
            throw new IllegalArgumentException("Name can't have spaces!");
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

                if (this.commands.containsKey(alias)) {
                    throw new IllegalArgumentException(String.format(
                        "Alias %s already present for command (Stored for: %s, trying to insert: %s))",
                        alias,
                        this.commands.get(alias).getClass().getSimpleName(),
                        command.getClass().getSimpleName()
                    ));
                }
            }

            for (final String alias : lowerAliasses) {
                this.aliases.put(alias, command.getName());
            }
        }

        this.commands.put(cmdName, command);
    }
}
