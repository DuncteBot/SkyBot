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

package ml.duncte123.skybot.listeners;

import io.sentry.Sentry;
import kotlin.Triple;
import me.duncte123.botcommons.BotCommons;
import me.duncte123.botcommons.messaging.MessageConfig;
import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import com.dunctebot.models.settings.GuildSetting;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.PerspectiveApi;
import ml.duncte123.skybot.utils.SpamFilter;
import ml.duncte123.skybot.web.WebSocketClient;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.AirUtils.setJDAContext;
import static ml.duncte123.skybot.utils.CommandUtils.isDev;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

public abstract class MessageListener extends BaseListener {

    protected final CommandManager commandManager = variables.getCommandManager();
    private static final String PROFANITY_DISABLE = "--no-filter";
    /* package */ final SpamFilter spamFilter = new SpamFilter(variables);
    /* package */ final ScheduledExecutorService systemPool = Executors.newScheduledThreadPool(4,
        (r) -> new Thread(r, "Bot-Service-Thread"));

    /* package */ MessageListener(Variables variables) {
        super(variables);
    }

    /* package */ void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (topicContains(event.getChannel(), PROFANITY_DISABLE)) {
            return;
        }

        this.handlerThread.submit(() -> {
            final DunctebotGuild guild = new DunctebotGuild(event.getGuild(), variables);
            final GuildSetting settings = guild.getSettings();

            if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) &&
                !Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {

                if (blacklistedWordCheck(guild, event.getMessage(), event.getMember(), settings.getBlacklistedWords())) {
                    return;
                }

                checkSwearFilter(event.getMessage(), event, guild);
            }
        });
    }

    /* package */ void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        final Guild guild = event.getGuild();

        if (isBotfarm(guild)) {
            return;
        }

        // This happens?
        if (event.getMember() == null && !event.isWebhookMessage()) {
            Sentry.capture(new Exception(String.format(
                "Got null member for no webhook message (what the fuck):\n Event:GuildMessageReceivedEvent\nMember:%s\nMessage:%s\nAuthor:%s (bot %s)",
                event.getMember(),
                event.getMessage(),
                event.getAuthor(),
                event.getAuthor().isBot()
            )));
        }

        if (event.getAuthor().isBot() ||
            event.isWebhookMessage() ||
            event.getMember() == null // Just in case Discord fucks up *again*
        ) {
            return;
        }

        final String raw = event.getMessage().getContentRaw().trim();

        if (raw.equals(Settings.PREFIX + "shutdown") && isDev(event.getAuthor().getIdLong())) {
            LOGGER.info("Initialising shutdown!!!");

            final ShardManager manager = Objects.requireNonNull(event.getJDA().getShardManager());

            event.getMessage().addReaction(MessageUtils.getSuccessReaction()).queue(
                success -> killAllShards(manager, true),
                failure -> killAllShards(manager, true)
            );

            return;
        }

        this.handlerThread.submit(() -> {
            try {
                setJDAContext(event.getJDA());
                handleMessageEventChecked(raw, guild, event);
            }
            catch (Exception e) {
                Sentry.capture(e);
                e.printStackTrace();
            }
        });
    }

    private boolean invokeAutoResponse(List<CustomCommand> autoResponses, String[] split, GuildMessageReceivedEvent event) {
        final String stripped = event.getMessage().getContentStripped().toLowerCase();

        final Optional<CustomCommand> match = autoResponses.stream()
            .filter((cmd) -> stripped.contains(cmd.getName().toLowerCase())).findFirst();

        if (match.isPresent()) {
            final CustomCommand cmd = match.get();

            commandManager.dispatchCommand(cmd, "", Arrays.asList(split).subList(1, split.length), event);
            return true;
        }

        return false;
    }

    private void handleMessageEventChecked(String raw, Guild guild, GuildMessageReceivedEvent event) {
        final GuildSetting settings = GuildSettingsUtils.getGuild(guild.getIdLong(), this.variables);
        final String customPrefix = settings.getCustomPrefix();

        if (!commandManager.isCommand(customPrefix, raw) && doAutoModChecks(event, settings, raw)) {
            return;
        }

        final User selfUser = event.getJDA().getSelfUser();
        final String selfRegex = "<@!?" + selfUser.getId() + '>';

        if (raw.matches(selfRegex)) {
            sendMsg(
                new MessageConfig.Builder()
                    .setChannel(event.getChannel())
                    .setMessageFormat(
                        "Hey %s, try `%shelp` for a list of commands. If it doesn't work scream at _duncte123#1245_",
                        event.getAuthor(),
                        customPrefix)
                    .build()
            );
            return;
        }

        final String[] split = raw.replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+");
        final List<CustomCommand> autoResponses = commandManager.getAutoResponses(guild.getIdLong());

        if (!autoResponses.isEmpty() && invokeAutoResponse(autoResponses, split, event)) {
            return;
        }

        if (doesNotStartWithPrefix(event, raw, customPrefix) || !canRunCommands(raw, customPrefix, event)) {
            return;
        }

        if (raw.matches(selfRegex + "(.*)")) {
            //Handle the chat command
            Objects.requireNonNull(commandManager.getCommand("chat")).executeCommand(new CommandContext(
                "chat",
                Arrays.asList(split).subList(1, split.length),
                event,
                variables
            ));
        } else {
            //Handle the command
            commandManager.runCommand(event, customPrefix);
        }
    }

    private boolean doesNotStartWithPrefix(GuildMessageReceivedEvent event, String raw, String customPrefix) {
        final String rwLower = raw.toLowerCase();

        if (rwLower.startsWith(Settings.OTHER_PREFIX.toLowerCase())) {
            return false;
        }

        if (rwLower.startsWith(Settings.PREFIX.toLowerCase())) {
            return false;
        }

        if (rwLower.startsWith(customPrefix)) {
            return false;
        }

        final String selfMember = event.getGuild().getSelfMember().getAsMention();

        if (rwLower.startsWith(selfMember)) {
            return false;
        }

        final String selfUser = event.getJDA().getSelfUser().getAsMention();

        return !rwLower.startsWith(selfUser);
    }

    private boolean shouldBlockCommand(@Nonnull String customPrefix, @Nonnull String raw, @Nonnull String input) {
        return input.equalsIgnoreCase(
            raw.replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(customPrefix), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0]
        );
    }

    private boolean hasCorrectCategory(@Nonnull String raw, @Nonnull String categoryName, @Nonnull String customPrefix) {

        final ICommand command = commandManager.getCommand(
            raw.replaceFirst(Pattern.quote(customPrefix), Settings.PREFIX)
                .replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Settings.PREFIX)
                .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase());

        if (command == null) {
            return false;
        }

        return command.getCategory() == CommandCategory.valueOf(categoryName.toUpperCase());
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isCategory(@Nonnull String name) {
        try {
            return CommandCategory.valueOf(name.toUpperCase()) != null;
        }
        catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private boolean canRunCommands(String raw, String customPrefix, @Nonnull GuildMessageReceivedEvent event) {

        final String topic = event.getChannel().getTopic();

        if (topic == null || topic.isEmpty()) {
            return true;
        }

        if (topicContains(event.getChannel(), "-commands")) {
            return false;
        }

        final String[] blocked = topic.split("-");

        for (final String item : blocked) {
            String string = item;

            if (string.charAt(0) == '!') {
                string = string.split("!")[1];

                if (isCategory(string.toUpperCase()) && !hasCorrectCategory(raw, string, customPrefix)) {
                    return false;
                }

                return !shouldBlockCommand(customPrefix, raw, string);
            }

            if (isCategory(string.toUpperCase()) && hasCorrectCategory(raw, string, customPrefix)) {
                return false;
            }

            if (shouldBlockCommand(customPrefix, raw, string)) {
                return false;
            }

        }

        return true;
    }

    /// <editor-fold desc="auto moderation" defaultstate="collapsed">
    private void checkMessageForInvites(Guild guild, GuildMessageReceivedEvent event, GuildSetting settings, String raw) {
        if (settings.isFilterInvites() && guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            final Matcher matcher = Message.INVITE_PATTERN.matcher(raw);
            if (matcher.find()) {
                //Get the invite Id from the message
                final String inviteID = matcher.group(matcher.groupCount());

                //Prohibiting failure because the bot is currently banned from the other guild.
                guild.retrieveInvites().queue((invites) -> {
                    //Check if the invite is for this guild, if it is not delete the message
                    if (invites.stream().noneMatch((invite) -> invite.getCode().equals(inviteID))) {
                        event.getMessage().delete().reason("Contained unauthorized invite.").queue((it) ->
                                sendMsg(MessageConfig.Builder.fromEvent(event)
                                    .setMessage(event.getAuthor().getAsMention() + ", please don't post invite links here.")
                                    .setSuccessAction(m -> m.delete().queueAfter(4, TimeUnit.SECONDS))
                                    .build()),
                            (t) -> {}
                        );
                    }
                });
            }
        }
    }

    private boolean checkSwearFilter(Message messageToCheck, GenericGuildMessageEvent event, DunctebotGuild guild) {
        final GuildSetting settings = guild.getSettings();

        if (settings.isEnableSwearFilter() && !topicContains(event.getChannel(), PROFANITY_DISABLE)) {
            final float score = PerspectiveApi.checkSwearFilter(
                messageToCheck.getContentRaw(),
                event.getChannel().getId(),
                variables.getConfig().apis.googl,
                settings.getFilterType(),
                variables.getJackson());

            // if the score is less than our target value it's not swearing
            if (score < settings.getAiSensitivity()) {
                return false;
            }

            final String display = messageToCheck.getContentDisplay();

            messageToCheck.delete()
                .reason("Blocked for swearing: " + display)
                .queue(null, (t) -> {});

            sendMsg(new MessageConfig.Builder()
                .setChannel(event.getChannel())
                .setMessageFormat(
                    "Hello there, %s please do not use cursive language within this Discord.",
                    messageToCheck.getAuthor().getAsMention()
                )
                .setSuccessAction(
                    (m) -> m.delete().queueAfter(5, TimeUnit.SECONDS, null, (t) -> {})
                )
                    .build());

            modLog(String.format(
                "Message with score %.2f by %#s deleted in %s for profanity, message content was:```\n%s```",
                score,
                messageToCheck.getAuthor(),
                event.getChannel().getAsMention(),
                StringUtils.abbreviate(display, Message.MAX_CONTENT_LENGTH - 150) // Just to be safe
            ), guild);

            return true;
        }

        return false;
    }

    private boolean blacklistedWordCheck(DunctebotGuild dbG, Message messageToCheck, Member member, List<String> blacklist) {
        if (member.hasPermission(Permission.KICK_MEMBERS)) {
            return false;
        }

        final String raw = messageToCheck.getContentRaw().toLowerCase();

        for (final String foundWord : blacklist) {
            if (Pattern.compile("\\b" + foundWord + "\\b").matcher(raw).find()) {
                messageToCheck.delete()
                    .reason(String.format("Contains blacklisted word: \"%s\"", foundWord)).queue();

                modLog(String.format(
                    "Deleted message from %#s in %s for containing the blacklisted word \"%s\"",
                    messageToCheck.getAuthor(),
                    messageToCheck.getChannel(),
                    foundWord
                ), dbG);

                sendMsg(new MessageConfig.Builder()
                    .setChannel((TextChannel) messageToCheck.getChannel())
                    .setMessageFormat(
                        "%s the word \"%s\" is blacklisted on this server",
                        messageToCheck.getMember(),
                        foundWord
                    )
                    .setSuccessAction(
                        (m) -> m.delete().queueAfter(5, TimeUnit.SECONDS, null, (t) -> {})
                    )
                    .build());

                return true;
            }
        }

        return false;
    }

    private void checkSpamFilter(Message messageToCheck, GuildMessageReceivedEvent event, GuildSetting settings, DunctebotGuild guild) {
        if (settings.isEnableSpamFilter()) {
            final long[] rates = settings.getRatelimits();

            spamFilter.applyRates(rates);

            if (spamFilter.check(new Triple<>(event.getMember(), messageToCheck, settings.getKickState()))) {
                modLog(event.getJDA().getSelfUser(), event.getAuthor(),
                    settings.getKickState() ? "kicked" : "muted", "spam", guild);
            }
        }
    }

    private boolean doAutoModChecks(@Nonnull GuildMessageReceivedEvent event, GuildSetting settings, String raw) {
        final Guild guild = event.getGuild();
        if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)
            && !Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {

            checkMessageForInvites(guild, event, settings, raw);

            final Message messageToCheck = event.getMessage();
            final DunctebotGuild dbG = new DunctebotGuild(event.getGuild(), variables);

            if (blacklistedWordCheck(dbG, messageToCheck, event.getMember(), settings.getBlacklistedWords())) {
                return true;
            }

            if (checkSwearFilter(messageToCheck, event, dbG)) {
                return true;
            }

            checkSpamFilter(messageToCheck, event, settings, dbG);
        }

        return false;
    }
    /// </editor-fold>

    private boolean topicContains(TextChannel channel, String search) {
        final String topic = channel.getTopic();

        if (topic == null || topic.isBlank()) {
            return false;
        }

        return topic.contains(search);
    }

    public void killAllShards(@Nonnull ShardManager manager, boolean kill) {
        final Thread shutdownThread = new Thread(() -> {
            try {
                manager.getShardCache().forEach((jda) -> jda.setEventManager(null));

                // Sleep for 3 seconds
                TimeUnit.SECONDS.sleep(3);

                // Kill all threads
                this.systemPool.shutdown();

                final WebSocketClient client = SkyBot.getInstance().getWebsocketClient();

                if (client != null) {
                    client.shutdown();
                }

                AirUtils.stop(variables.getAudioUtils(), manager);
                BotCommons.shutdown(manager);

                // There are *some* applications (weeb.java *cough*) that are stupid
                // and do not allow us to shut down okhttp or create deamon threads
                if (kill) {
                    System.exit(0);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, "shutdown-thread");
        shutdownThread.start();
    }
}
