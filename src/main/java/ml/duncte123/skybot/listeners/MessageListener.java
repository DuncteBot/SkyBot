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

package ml.duncte123.skybot.listeners;

import com.dunctebot.models.settings.GuildSetting;
import io.sentry.Sentry;
import kotlin.Triple;
import me.duncte123.botcommons.BotCommons;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageConfig;
import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.database.RedisConnection;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import ml.duncte123.skybot.objects.command.CustomCommand;
import ml.duncte123.skybot.objects.discord.MessageData;
import ml.duncte123.skybot.objects.user.UnknownUser;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.PerspectiveApi;
import ml.duncte123.skybot.utils.SpamFilter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.AirUtils.setJDAContext;
import static ml.duncte123.skybot.utils.CommandUtils.isDev;
import static ml.duncte123.skybot.utils.CommandUtils.isGuildPatron;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;
import static net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE;

public abstract class MessageListener extends BaseListener {

    protected final RedisConnection redis = new RedisConnection();
    protected final CommandManager commandManager = variables.getCommandManager();
    private static final String PROFANITY_DISABLE = "--no-filter";
    protected final SpamFilter spamFilter = new SpamFilter(variables);
    protected final ScheduledExecutorService systemPool = Executors.newScheduledThreadPool(4,
        (r) -> new Thread(r, "Bot-Service-Thread"));

    protected MessageListener(Variables variables) {
        super(variables);
    }

    protected void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        // ignore bots
        final Message message = event.getMessage();
        final User author = message.getAuthor();

        if (author.isBot() || author.isSystem() || message.isWebhookMessage() || event.getMember() == null) {
            return;
        }

        if (topicContains(event.getChannel(), PROFANITY_DISABLE)) {
            return;
        }

        this.handlerThread.submit(() -> {
            try {
                final DunctebotGuild guild = new DunctebotGuild(event.getGuild(), variables);
                final GuildSetting settings = guild.getSettings();

                if (settings.isMessageLogging()) {
                    final MessageData edited = MessageData.from(message);
                    final MessageData original = this.redis.getAndUpdateMessage(message.getId(), edited, isGuildPatron(guild));

                    // data will be null if the message expired
                    if (original != null) {
                        this.logEditedMessage(original, edited, guild);
                    }
                }

                if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) &&
                    !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {

                    if (blacklistedWordCheck(guild, message, event.getMember(), settings.getBlacklistedWords())) {
                        return;
                    }

                    checkSwearFilter(message, event, guild);
                }
            } catch (Exception e) {
                LOGGER.error("Exception on message update", e);
            }
        });
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    protected void onMessageBulkDelete(final MessageBulkDeleteEvent event) {
        this.handlerThread.submit(() -> {
            try {
                final DunctebotGuild guild = new DunctebotGuild(event.getGuild(), variables);

                if (!guild.getSettings().isMessageLogging()) {
                    // just delete the message here as we don't want to keep it around
                    this.redis.deleteMessages(event.getMessageIds());
                    return;
                }

                final List<MessageData> dataList = this.redis.getAndDeleteMessages(event.getMessageIds());
                final StringBuilder builder = new StringBuilder();
                // temporarily store the users to prevent spamming discord for the data
                final Map<Long, User> tmpUsers = new HashMap<>();
                final JDA jda = event.getJDA();

                // reverse the list to preserve the correct order
                Collections.reverse(dataList);

                for (final MessageData data : dataList) {
                    final long authorId = data.getAuthorId();

                    final Consumer<User> userConsumer = (user) -> {
                        builder.append('[')
                            .append(data.getCratedAt().format(DateTimeFormatter.RFC_1123_DATE_TIME))
                            .append("] (")
                            .append(user.getAsTag())
                            .append(" - ")
                            .append(user.getIdLong())
                            .append(") [")
                            .append(data.getMessageId())
                            .append("]: ")
                            .append(data.getContent())
                            .append('\n');
                    };

                    if (tmpUsers.containsKey(authorId)) {
                        userConsumer.accept(tmpUsers.get(authorId));
                    } else {
                        // try to fetch the user since we don't cache them
                        // calls are sequential making sure the messages are still in order
                        jda.retrieveUserById(authorId).queue(
                            (user) -> {
                                tmpUsers.put(authorId, user);
                                userConsumer.accept(user);
                            },
                            (error) -> userConsumer.accept(new UnknownUser(authorId))
                        );
                    }
                }

                final TextChannel channel = event.getChannel();
                final EmbedBuilder embed = EmbedUtils.embedField(
                        "Bulk Delete",
                        "Bulk deleted messages from <#%s> are available in the attached file.".formatted(channel.getIdLong())
                    )
                    .setColor(0xE67E22)
                    .setTimestamp(Instant.now());

                modLog(
                    new MessageConfig.Builder()
                        .addEmbed(true, embed)
                        .setActionConfig(
                            (action) -> action.addFile(
                                builder.toString().getBytes(),
                                "bulk_delete_%s.txt".formatted(System.currentTimeMillis())
                            )
                        ),
                    guild
                );
            } catch (Exception e) {
                LOGGER.error("Exception on message bulk delete", e);
            }
        });
    }

    protected void onGuildMessageDelete(final GuildMessageDeleteEvent event) {
        this.handlerThread.submit(() -> {
            try {
                final DunctebotGuild guild = new DunctebotGuild(event.getGuild(), variables);

                if (!guild.getSettings().isMessageLogging()) {
                    // just delete the message here as we don't want to keep it around
                    this.redis.deleteMessage(event.getMessageId());
                    return;
                }

                final MessageData data = this.redis.getAndDeleteMessage(event.getMessageId());

                if (data != null) {
                    this.logDeletedMessage(data, guild);
                }
            } catch (Exception e) {
                LOGGER.error("Exception on message delete", e);
            }
        });
    }

    protected void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        final Guild guild = event.getGuild();

        if (isBotfarm(guild)) {
            return;
        }

        // This happens?
        final User author = event.getAuthor();
        if (event.getMember() == null && !event.isWebhookMessage()) {
            final Exception weirdEx = new Exception(String.format(
                "Got null member for no webhook message (what the fuck):\n Event:GuildMessageReceivedEvent\nMember:%s\nMessage:%s\nAuthor:%s (bot %s)",
                event.getMember(),
                event.getMessage(),
                author,
                author.isBot()
            ));

            LOGGER.error("Error with message listener", weirdEx);
            Sentry.captureException(weirdEx);
        }

        if (author.isBot() || author.isSystem() ||
            event.isWebhookMessage() ||
            event.getMember() == null // Just in case Discord fucks up *again*
        ) {
            return;
        }

        final String raw = event.getMessage().getContentRaw().trim();

        if (raw.equals(Settings.PREFIX + "shutdown") && isDev(author.getIdLong())) {
            LOGGER.info("Initialising shutdown!!!");

            final ShardManager manager = Objects.requireNonNull(event.getJDA().getShardManager());

            event.getMessage().addReaction(MessageUtils.getSuccessReaction()).queue(
                success -> shutdownBot(manager),
                failure -> shutdownBot(manager)
            );

            return;
        }

        this.handlerThread.submit(() -> {
            try {
                setJDAContext(event.getJDA());
                handleMessageEventChecked(raw, guild, event);
            } catch (Exception e) {
                Sentry.captureException(e);
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
        final Message message = event.getMessage();

        if (settings.isMessageLogging()){
            final MessageData data = MessageData.from(message);

            this.redis.storeMessage(data, isGuildPatron(guild));
        }

        if (!commandManager.isCommand(customPrefix, raw) && doAutoModChecks(event, settings, raw)) {
            return;
        }

        final User selfUser = event.getJDA().getSelfUser();
        final long selfId = selfUser.getIdLong();
        final String selfRegex = "<@!?" + selfId + '>';

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

        if (doesNotStartWithPrefix(selfId, raw, customPrefix) || !canRunCommands(raw, customPrefix, event)) {
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

    private boolean doesNotStartWithPrefix(long selfId, String raw, String customPrefix) {
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

        return !raw.matches("^<@!?" + selfId + "?.*$");
    }

    private String getCommandName(@Nonnull String customPrefix, @Nonnull String raw) {
        return raw.replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Settings.PREFIX)
            .replaceFirst(Pattern.quote(customPrefix), Settings.PREFIX)
            .replaceFirst(Pattern.quote(Settings.PREFIX), "")
            .split("\\s+", 2)[0];
    }

    private boolean shouldBlockCommand(@Nonnull String customPrefix, @Nonnull String raw, @Nonnull String input) {
        return input.equalsIgnoreCase(getCommandName(customPrefix, raw));
    }

    private boolean hasCorrectCategory(@Nonnull String raw, @Nonnull String categoryName, @Nonnull String customPrefix) {
        final ICommand command = commandManager.getCommand(
            getCommandName(customPrefix, raw).toLowerCase()
        );

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
            if (item.isBlank()) {
                continue;
            }

            String string = item;

            if (!string.isEmpty() && string.charAt(0) == '!') {
                string = string.substring(1);

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

                // TODO: use the invite cache to check if invite is cached before retrieving
                //Prohibiting failure because the bot is currently banned from the other guild.
                guild.retrieveInvites().queue((invites) -> {
                    //Check if the invite is for this guild, if it is not delete the message
                    if (invites.stream().noneMatch((invite) -> invite.getCode().equals(inviteID))) {
                        event.getMessage().delete().reason("Contained unauthorized invite.").queue((it) ->
                                sendMsg(MessageConfig.Builder.fromEvent(event)
                                    .setMessage(event.getAuthor().getAsMention() + ", please don't post invite links here.")
                                    .setSuccessAction(m -> m.delete().queueAfter(4, TimeUnit.SECONDS))
                                    .build()),
                            new ErrorHandler().ignore(UNKNOWN_MESSAGE, MISSING_PERMISSIONS)
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
                .queue(null, new ErrorHandler().ignore(UNKNOWN_MESSAGE, MISSING_PERMISSIONS));

            sendMsg(new MessageConfig.Builder()
                .setChannel(event.getChannel())
                .setMessageFormat(
                    // TODO: allow patrons to customise this message
                    "Hello there, %s please do not use cursive language within this server.",
                    messageToCheck.getAuthor().getAsMention()
                )
                .setSuccessAction(
                    (m) -> m.delete().queueAfter(5, TimeUnit.SECONDS,
                        null, new ErrorHandler().ignore(UNKNOWN_MESSAGE, MISSING_PERMISSIONS))
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
                        (m) -> m.delete().queueAfter(5, TimeUnit.SECONDS,
                            null, new ErrorHandler().ignore(UNKNOWN_MESSAGE))
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
                    settings.getKickState() ? "kicked" : "muted", "spam", null, guild);
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

    public void shutdownBot(@Nullable ShardManager manager) {
        if (manager != null) {
            BotCommons.shutdown(manager);
        }
    }

    private void logEditedMessage(MessageData original, MessageData edited, DunctebotGuild guild) {
        // I would not expect this to happen, but we're still working with discord here
        // At this point I am expecting update events for messages that are not edited
        if (!edited.isEdit()) {
            return;
        }

        final Consumer<User> userConsumer = (user) -> {
            final EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed()
                .setColor(0xF1C40F)
                .setAuthor(
                    "%s (%s)".formatted(user.getAsTag(), edited.getAuthorId()),
                    "https://duncte.bot/patreon",
                    user.getEffectiveAvatarUrl().replace(".gif", ".png")
                )
                .setDescription(
                    "Message %s edited in <#%s> ([link](%s))\n**Before:** %s\n**After:** %s".formatted(
                        edited.getMessageId(),
                        edited.getChannelId(),
                        edited.getJumpUrl(guild.getIdLong()),
                        MarkdownSanitizer.escape(original.getContent(), true),
                        MarkdownSanitizer.escape(edited.getContent(), true)
                    )
                )
                .setTimestamp(Instant.now());

            if (!edited.getAttachments().isEmpty()) {
                embedBuilder.addField(
                    "Attachments",
                    edited.getAttachments()
                        .stream()
                        .map((a) -> "[View](" + a + ')')
                        .collect(Collectors.joining(" ")),
                    false
                );
            }

            modLog(
                new MessageConfig.Builder().setEmbeds(true, embedBuilder),
                guild
            );
        };

        // try to fetch the user since we don't cache them
        guild.getJDA().retrieveUserById(edited.getAuthorId()).queue(
            userConsumer,
            (error) -> userConsumer.accept(new UnknownUser(edited.getAuthorId()))
        );
    }

    private void logDeletedMessage(MessageData data, DunctebotGuild guild) {
        final Consumer<User> userConsumer = (user) -> {
            final EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed()
                .setColor(0xFF0000)
                .setAuthor(
                    "%s (%s)".formatted(user.getAsTag(), data.getAuthorId()),
                    "https://duncte.bot/patreon",
                    user.getEffectiveAvatarUrl().replace(".gif", ".png")
                )
                .setDescription(
                    "Message %s deleted from <#%s> ([link](%s))\n**Content:** %s".formatted(
                        data.getMessageId(),
                        data.getChannelId(),
                        data.getJumpUrl(guild.getIdLong()),
                        MarkdownSanitizer.escape(data.getContent(), true)
                    )
                )
                .setTimestamp(Instant.now());

            if (!data.getAttachments().isEmpty()) {
                embedBuilder.addField(
                    "Attachments",
                    data.getAttachments()
                        .stream()
                        .map((a) -> "[View](" + a + ')')
                        .collect(Collectors.joining(" ")),
                    false
                );
            }

            modLog(
                new MessageConfig.Builder().setEmbeds(true, embedBuilder),
                guild
            );
        };

        // try to fetch the user since we don't cache them
        guild.getJDA().retrieveUserById(data.getAuthorId()).queue(
            userConsumer,
            (error) -> userConsumer.accept(new UnknownUser(data.getAuthorId()))
        );
    }
}
