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

package ml.duncte123.skybot;

import com.jagrosh.jagtag.Parser;
import gnu.trove.map.TObjectLongMap;
import io.sentry.Sentry;
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
import ml.duncte123.skybot.commands.utils.EnlargeCommand;
import ml.duncte123.skybot.commands.utils.RoleInfoCommand;
import ml.duncte123.skybot.commands.weeb.*;
import ml.duncte123.skybot.objects.command.*;
import ml.duncte123.skybot.objects.pairs.LongLongPair;
import ml.duncte123.skybot.utils.CommandUtils;
import ml.duncte123.skybot.utils.MapUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import static net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL;

public class CommandManager {
    private static final TObjectLongMap<String> COOLDOWNS = MapUtils.newObjectLongMap();
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);
    private static final Pattern COMMAND_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
    private static final ScheduledExecutorService COOLDOWN_THREAD = Executors.newSingleThreadScheduledExecutor((r) -> {
        final Thread thread = new Thread(r, "Command-cooldown-thread");
        thread.setDaemon(true);
        return thread;
    });
    private final ExecutorService commandThread = Executors.newCachedThreadPool((r) -> {
        final Thread thread = new Thread(r, "Command-execute-thread");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<String, ICommand<CommandContext>> commands = new ConcurrentHashMap<>();
    private final Map<String, String> aliases = new ConcurrentHashMap<>();
    private final Set<CustomCommand> customCommands = ConcurrentHashMap.newKeySet();
    private final Variables variables;

    static {
        COOLDOWN_THREAD.scheduleWithFixedDelay(() -> {
            try {
                // Loop over all cooldowns with a 5 minute interval
                // This makes sure that we don't have any useless cooldowns in the system hogging up memory
                COOLDOWNS.forEachEntry((key, val) -> {
                    final long remaining = calcTimeRemaining(val);

                    // Remove the value from the cooldowns if it is less or equal to 0
                    if (remaining <= 0) {
                        COOLDOWNS.remove(key);
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

        /// <editor-fold desc="Command Loading" defaultstate="collapsed">
        this.addCommand(new AchievementCommand());
        this.addCommand(new AdviceCommand());
        this.addCommand(new AlpacaCommand());
        this.addCommand(new AnnounceCommand());
        this.addCommand(new AutoBanBypassCommand());
        this.addCommand(new AutoRoleCommand());
        this.addCommand(new AvatarCommand());
        this.addCommand(new B1nzyCommand());
        this.addCommand(new BanCommand());
        this.addCommand(new BassBoostCommand());
        this.addCommand(new BirbCommand());
        this.addCommand(new BlackAndWhiteCommand());
        this.addCommand(new BlackListCommand());
        this.addCommand(new BlurCommand());
        this.addCommand(new BotinfoCommand());
        this.addCommand(new CaptchaCommand());
        this.addCommand(new CarsAndHentaiCommand());
        this.addCommand(new ChangeLogCommand());
        this.addCommand(new ChatCommand());
        this.addCommand(new CleanupCommand());
        this.addCommand(new ClearLeftGuildsCommand());
        this.addCommand(new ClintCommand());
        this.addCommand(new CommandDumpCommand());
        this.addCommand(new CoinCommand());
        this.addCommand(new ColorCommand());
        this.addCommand(new CookieCommand());
        this.addCommand(new CSShumorCommand());
        this.addCommand(new CustomCommandCommand());
        this.addCommand(new DanceCommand());
        this.addCommand(new DeepfryCommand());
        this.addCommand(new DeHoistCommand());
        this.addCommand(new DeletCommand());
        this.addCommand(new DeleteCommand());
        this.addCommand(new DidYouMeanCommand());
        this.addCommand(new DiscordMemesCommand());
        this.addCommand(new DogCommand());
        this.addCommand(new DonateCommand());
        this.addCommand(new DrakeCommand());
        this.addCommand(new EmoteCommand());
        this.addCommand(new EnlargeCommand());
        this.addCommand(new EvalCommand());
        this.addCommand(new EveryoneCommand());
        this.addCommand(new ExplosmCommand());
        this.addCommand(new FactsCommand());
        this.addCommand(new FakeWordCommand());
        this.addCommand(new FlagCommand());
        this.addCommand(new ForceDisconnectCommand());
        this.addCommand(new ForceSkip());
        this.addCommand(new GuildInfoCommand());
        this.addCommand(new GuildJoinsCommand());
        this.addCommand(new HackbanCommand());
        this.addCommand(new HelpCommand());
        this.addCommand(new HentaiCommand());
        this.addCommand(new HugCommand());
        this.addCommand(new InvertCommand());
        this.addCommand(new InviteCommand());
        this.addCommand(new IssueCommand());
        this.addCommand(new ItsFreeRealEstateCommand());
        this.addCommand(new IWantToDieCommand());
        this.addCommand(new JoinCommand());
        this.addCommand(new JokeCommand());
        this.addCommand(new JokeoverheadCommand());
        this.addCommand(new KickCommand());
        this.addCommand(new KickMeCommand());
        this.addCommand(new KittyCommand());
        this.addCommand(new LeaveCommand());
        this.addCommand(new LewdCommand());
        this.addCommand(new LewdKitsuneCommand());
        this.addCommand(new LewdNekoCommand());
        this.addCommand(new LickCommand());
        this.addCommand(new LinusCommand());
        this.addCommand(new LlamaCommand());
        this.addCommand(new LoadCommand());
        this.addCommand(new LoadingBarCommand());
        this.addCommand(new LockEmoteCommand());
        this.addCommand(new LoveCommand());
        this.addCommand(new LyricsCommand());
        this.addCommand(new MeguminCommand());
        this.addCommand(new MemeCommand());
        this.addCommand(new MuteCommand());
        this.addCommand(new MuteRoleCommand());
        this.addCommand(new NowPlayingCommand());
        this.addCommand(new OldestCommand());
        this.addCommand(new OrlyCommand());
        this.addCommand(new OwoCommand());
        this.addCommand(new PatCommand());
        // TODO: remove some day, can't do any harm really
        this.addCommand(new PatreonCheckCommand());
        this.addCommand(new PauseCommand());
        this.addCommand(new PcCheckCommand());
        this.addCommand(new PingCommand());
        this.addCommand(new PixelateCommand());
        this.addCommand(new PlayCommand());
        this.addCommand(new PlayRawCommand());
        this.addCommand(new PornHubCommand());
        this.addCommand(new PPlayCommand());
        this.addCommand(new PronounsCheckCommand());
        this.addCommand(new PunchCommand());
        this.addCommand(new PurgeChannelCommand());
        this.addCommand(new PurgeUserCommand());
        this.addCommand(new QueueCommand());
        this.addCommand(new QuoteCommand());
        this.addCommand(new RadioCommand());
        this.addCommand(new ReaddCommand());
        this.addCommand(new RemindersCommand());
        this.addCommand(new RemindmeCommand());
        this.addCommand(new RepeatCommand());
        this.addCommand(new RepeatQueueCommand());
        this.addCommand(new RestartCommand());
        this.addCommand(new RestartShardCommand());
        this.addCommand(new ReverseCommand());
        this.addCommand(new RoleInfoCommand());
        this.addCommand(new RolesCommand());
        this.addCommand(new SaltyCommand());
        this.addCommand(new SaturateCommand());
        this.addCommand(new SaveCommand());
        this.addCommand(new ScreenshotCommand());
        this.addCommand(new ScrollCommand());
        this.addCommand(new SealCommand());
        this.addCommand(new SearchCommand());
        this.addCommand(new SeekCommand());
        this.addCommand(new SetColorCommand());
        this.addCommand(new SetDescriptionCommand());
        this.addCommand(new SetJoinMessageCommand());
        this.addCommand(new SetLeaveMessageCommand());
        this.addCommand(new SetLogChannelCommand());
        this.addCommand(new SetPrefixCommand());
        this.addCommand(new SetPronounsCommand());
        this.addCommand(new SetRateLimitsCommand());
        this.addCommand(new SettingsCommand());
        this.addCommand(new SetWelcomeChannelCommand());
        this.addCommand(new ShardInfoCommand());
        this.addCommand(new ShitCommand());
        this.addCommand(new ShootCommand());
        this.addCommand(new ShortenCommand());
        this.addCommand(new ShrugCommand());
        this.addCommand(new ShuffleCommand());
        this.addCommand(new SkipCommand());
        this.addCommand(new SlowModeCommand());
        this.addCommand(new SoftbanCommand());
        this.addCommand(new SPLookupCommand());
        this.addCommand(new StatsCommand());
        this.addCommand(new StopCommand());
        this.addCommand(new SuggestCommand());
        this.addCommand(new TagCommand(variables));
        this.addCommand(new TempBanCommand());
        this.addCommand(new TempMuteCommand());
        this.addCommand(new TestTagCommand());
        this.addCommand(new TheSearchCommand());
        this.addCommand(new ToggleAnnounceTracksCommand());
        this.addCommand(new ToggleAutoDehoistCommand());
        this.addCommand(new ToggleFilterInvitesCommand());
        this.addCommand(new ToggleJoinMessageCommand());
        this.addCommand(new ToggleKickModeCommand());
        this.addCommand(new ToggleSpamFilterCommand());
        this.addCommand(new ToggleSwearFilterCommand());
        this.addCommand(new TokenCommand());
        this.addCommand(new TranslateCommand());
        this.addCommand(new TrashCommand());
        this.addCommand(new UnbanCommand());
        this.addCommand(new UnlockEmoteCommand());
        this.addCommand(new UnmuteCommand());
        this.addCommand(new UnshortenCommand());
        this.addCommand(new UnwarnCommand());
        this.addCommand(new UptimeCommand());
        this.addCommand(new UrbanCommand());
        this.addCommand(new UserinfoCommand());
        this.addCommand(new VcAutoRoleCommand());
        this.addCommand(new VoiceKickCommand());
        this.addCommand(new VolumeCommand());
        this.addCommand(new VoteCommand());
        this.addCommand(new WarnCommand());
        this.addCommand(new WarningsCommand());
        this.addCommand(new WeebCommand());
        this.addCommand(new WolframAlphaCommand());
        this.addCommand(new XkcdCommand());
        this.addCommand(new YesNoCommand());
        this.addCommand(new YodaSpeakCommand());
        this.addCommand(new YoungestCommand());
        /// </editor-fold>

        loadCustomCommands();
    }

    public Collection<ICommand<CommandContext>> getCommands() {
        return this.commands.values();
    }

    public List<ICommand<CommandContext>> getCommands(CommandCategory category) {
        return this.commands.values()
            .stream()
            .filter((c) -> c.getCategory().equals(category))
            .collect(Collectors.toList());
    }

    /* package */ LongLongPair getCommandCount() {
        return new LongLongPair(this.commands.size(), this.aliases.size());
    }

    public Set<CustomCommand> getCustomCommands() {
        return this.customCommands;
    }

    @Nullable
    public ICommand<CommandContext> getCommand(String name) {

        ICommand<CommandContext> found = this.commands.get(name);

        if (found == null) {
            final String forAlias = this.aliases.get(name);

            if (forAlias != null) {
                found = this.commands.get(forAlias);
            }
        }

        return found;
    }

    public boolean isCommand(String customPrefix, String message) {
        final String[] split = message.toLowerCase()
            .replaceFirst(
                "(?i)" + Pattern.quote(Settings.PREFIX) + '|' + Pattern.quote(Settings.OTHER_PREFIX) + '|' + Pattern.quote(customPrefix),
                ""
            )
            .split("\\s+", 2);

        if (split.length >= 1) {
            final String invoke = split[0].toLowerCase();

            return getCommand(invoke) != null;
        }

        return false;
    }

    @Nullable
    public CustomCommand getCustomCommand(String invoke, long guildId) {
        return this.customCommands.stream()
            .filter((c) -> c.getGuildId() == guildId)
            .filter((c) -> c.getName().equalsIgnoreCase(invoke))
            .findFirst()
            .orElse(null);
    }

    public List<CustomCommand> getAutoResponses(long guildId) {
        return this.customCommands.stream()
            .filter((c) -> c.getGuildId() == guildId)
            .filter(CustomCommand::isAutoResponse)
            .collect(Collectors.toList());
    }

    public void runCommand(MessageReceivedEvent event, String customPrefix) {
        final String[] split = event.getMessage()
            .getContentRaw()
            .replaceFirst(
                "(?i)" + Pattern.quote(Settings.PREFIX) + '|' + Pattern.quote(Settings.OTHER_PREFIX) + '|' + Pattern.quote(customPrefix),
                ""
            )
            .trim()
            .split("\\s+", 2);
        final String invoke = split[0];

        final List<String> args = new ArrayList<>();

        if (split.length > 1) {
            final String raw = split[1];
            final Matcher matcher = COMMAND_PATTERN.matcher(raw);
            while (matcher.find()) {
                args.add(matcher.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
            }
        }

        dispatchCommand(invoke, invoke.toLowerCase(), args, event);
    }

    public void setCooldown(String key, int seconds) {
        COOLDOWNS.put(key, OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(seconds).toEpochSecond());
    }

    public long getRemainingCooldown(String key) {
        // If we don't have a cooldown for the command return 0
        if (!COOLDOWNS.containsKey(key)) {
            return 0;
        }

        // get the time that the cooldown started
        final long startTime = COOLDOWNS.get(key);
        // The time that is left until the cooldown is over
        final long timeLeft = calcTimeRemaining(startTime);

        // If the time is up we will return 0 and remove the keys from the cooldowns map
        if (timeLeft <= 0) {
            COOLDOWNS.remove(key);
            return 0;
        }

        return timeLeft;
    }

    private void dispatchCommand(String invoke, String invokeLower, List<String> args, MessageReceivedEvent event) {
        ICommand<?> cmd = getCommand(invokeLower);

        if (cmd == null) {
            cmd = getCustomCommand(invokeLower, event.getGuild().getIdLong());
        }

        if (cmd == null) {
            return;
        }

        dispatchCommand(cmd, invoke, args, event);
    }

    @SuppressWarnings("unchecked")
    public void dispatchCommand(@Nonnull ICommand<?> cmd, String invoke, List<String> args, MessageReceivedEvent event) {
        this.commandThread.submit(() -> {
            MDC.put("command.invoke", invoke);
            MDC.put("command.args", args.toString());
            MDC.put("user.tag", event.getAuthor().getAsTag());
            MDC.put("user.id", event.getAuthor().getId());
            MDC.put("guild", event.getGuild().toString());
            setJDAContext(event.getJDA());

            final MessageChannel channel = event.getChannel();

            if (!channel.canTalk()) {
                return;
            }

            // Suppress errors from when we can't type in the channel
            channel.sendTyping().queue(null, new ErrorHandler().ignore(UNKNOWN_CHANNEL, MISSING_ACCESS));

            try {
                if (cmd.isCustom()) {
                    runCustomCommand((ICommand<Object>) cmd, invoke, args, event);
                } else {
                    runNormalCommand((ICommand<CommandContext>) cmd, invoke, args, event);
                }
            }
            catch (Throwable ex) {
                Sentry.captureException(ex);
                LOGGER.error("Error while parsing command", ex);
                sendMsg(MessageConfig.Builder.fromEvent(event)
                    .setMessage("Something went wrong whilst executing the command, my developers have been informed of this\n" + ex.getMessage())
                    .build());
            }
        });
    }

    private boolean isSafeForWork(MessageReceivedEvent event) {
        final MessageChannelUnion channel = event.getChannel();

        if (channel instanceof IAgeRestrictedChannel nsfwChannel) {
            if (nsfwChannel.isNSFW()) {
                return false;
            }
        }

        final Guild.NSFWLevel nsfwLevel = event.getGuild().getNSFWLevel();

        return nsfwLevel == Guild.NSFWLevel.DEFAULT || nsfwLevel == Guild.NSFWLevel.SAFE;
    }

    private void runNormalCommand(ICommand<CommandContext> cmd, String invoke, List<String> args, MessageReceivedEvent event) {
        if (cmd.getCategory() == CommandCategory.NSFW && this.isSafeForWork(event)) {
            sendMsg(MessageConfig.Builder.fromEvent(event)
                .setMessage("Woops, this channel is not marked as NSFW.\n" +
                    "Please mark this channel as NSFW to use this command")
                .build());
            return;
        }

        MDC.put("command.class", cmd.getClass().getName());

        LOGGER.info("Dispatching command \"{}\" in guild \"{}\" with {}", cmd.gerNameForLogger(), event.getGuild(), args);

        cmd.executeCommand(
            new CommandContext(invoke, args, event, variables)
        );
    }

    private void runCustomCommand(ICommand<Object> cmd, String invoke, List<String> args, MessageReceivedEvent event) {
        final CustomCommand cusomCommand = (CustomCommand) cmd;

        if (cusomCommand.getGuildId() != event.getGuild().getIdLong()) {
            return;
        }

        try {
            MDC.put("command.custom.message", cusomCommand.getMessage());

            final Parser parser = CommandUtils.getParser(new CommandContext(invoke, args, event, variables));
            final String message = parser.parse(cusomCommand.getMessage());
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

                messageBuilder.addEmbed(true, embed);
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
            Sentry.captureException(e);
        }
    }

    private void loadCustomCommands() {
        this.variables.getDatabase().getCustomCommands().thenAccept((loadedCommands) -> {
            loadedCommands.forEach(
                (command) -> addCustomCommand(command, false, false)
            );
        });
    }

    public boolean editCustomCommand(CustomCommand cmd) {
        return addCustomCommand(cmd, true, true) == CommandResult.SUCCESS;
    }

    public CommandResult registerCustomCommand(CustomCommand cmd) {
        return addCustomCommand(cmd, true, false);
    }

    public CommandResult addCustomCommand(CustomCommand command, boolean insertInDb, boolean isEdit) {
        if (command.getName().contains(" ")) {
            throw new IllegalArgumentException("Name can't have spaces!");
        }

        final boolean commandFound = !isEdit && this.customCommands.stream()
            .anyMatch((cmd) -> cmd.getName().equalsIgnoreCase(command.getName()) && cmd.getGuildId() == command.getGuildId());

        if (commandFound) {
            return CommandResult.COMMAND_EXISTS;
        }

        final boolean limitReached = !isEdit && this.customCommands.stream().filter((cmd) -> cmd.getGuildId() == command.getGuildId()).count() >= 50;

        if (limitReached) {
            return CommandResult.LIMIT_REACHED;
        }

        if (insertInDb) {
            try {
                final CompletableFuture<CommandResult> future;

                if (isEdit) {
                    future = this.variables.getDatabase()
                        .updateCustomCommand(command.getGuildId(), command.getName(), command.getMessage(), command.isAutoResponse());
                } else {
                    future = this.variables.getDatabase()
                        .createCustomCommand(command.getGuildId(), command.getName(), command.getMessage());
                }

                final CommandResult res = future.get();

                if (res != null && res != CommandResult.SUCCESS) {
                    return res;
                }
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Sentry.captureException(e);
            }
        }

        if (isEdit) {
            this.customCommands.remove(getCustomCommand(command.getName(), command.getGuildId()));
        }

        this.customCommands.add(command);

        return CommandResult.SUCCESS;
    }

    public boolean removeCustomCommand(String name, long guildId) {
        return this.removeCustomCommand(name, guildId, true);
    }

    public boolean removeCustomCommand(String name, long guildId, boolean updateDB) {
        final CustomCommand cmd = getCustomCommand(name, guildId);

        if (cmd == null) {
            return false;
        }

        if (!updateDB) {
            this.customCommands.remove(cmd);
            return true;
        }

        try {
            final CompletableFuture<Boolean> future = this.variables.getDatabase().deleteCustomCommand(guildId, name);
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

    private void addCommand(ICommand<CommandContext> command) {
        final String name = command.getName();

        if (name.contains(" ")) {
            throw new IllegalArgumentException(" Name can't have spaces!");
        }

        final String cmdName = name.toLowerCase();

        if (this.commands.containsKey(cmdName)) {
            throw new IllegalArgumentException(String.format("Command %s already present", cmdName));
        }

        final List<String> lowerAliasses = Arrays.stream(command.getAliases()).map(String::toLowerCase).toList();

        if (!lowerAliasses.isEmpty()) {
            for (final String alias : lowerAliasses) {
                if (this.aliases.containsKey(alias)) {
                    throw new IllegalArgumentException(String.format(
                        "Alias %s already present (Stored for: %s, trying to insert: %s))",
                        alias,
                        this.aliases.get(alias),
                        name
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
                this.aliases.put(alias, name);
            }
        }

        this.commands.put(cmdName, command);
    }

    private static long calcTimeRemaining(long startTime) {
        // Get the start time as an OffsetDateTime
        final OffsetDateTime startTimeOffset = Instant.ofEpochSecond(startTime).atOffset(ZoneOffset.UTC);
        // get the time that is left for the cooldown
        return OffsetDateTime.now(ZoneOffset.UTC).until(startTimeOffset, ChronoUnit.SECONDS);
    }
}
