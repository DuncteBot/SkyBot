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

package ml.duncte123.skybot.listeners;

import io.sentry.Sentry;
import kotlin.Triple;
import me.duncte123.botcommons.BotCommons;
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
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.PerspectiveApi;
import ml.duncte123.skybot.utils.SpamFilter;
import ml.duncte123.skybot.web.WebRouter;
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
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormatAndDeleteAfter;
import static ml.duncte123.skybot.utils.AirUtils.setJDAContext;
import static ml.duncte123.skybot.utils.CommandUtils.isDev;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

public abstract class MessageListener extends BaseListener {

    protected final CommandManager commandManager = variables.getCommandManager();
    private static final String PROFANITY_FILTER_DISABLE_FLAG = "--no-filter";
    final SpamFilter spamFilter = new SpamFilter(variables);
    final ScheduledExecutorService systemPool = Executors.newScheduledThreadPool(4,
        (r) -> new Thread(r, "Bot-Service-Thread"));

    MessageListener(Variables variables) {
        super(variables);
    }

    void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (topicContains(event.getChannel(), PROFANITY_FILTER_DISABLE_FLAG)) {
            return;
        }

        this.handlerThread.submit(() -> {
            final DunctebotGuild guild = new DunctebotGuild(event.getGuild(), variables);

            if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) &&
                !Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE) &&
                guild.getSettings().isEnableSwearFilter()) {
                checkSwearFilter(event.getMessage(), event, guild);
            }
        });
    }

    void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        final Guild guild = event.getGuild();

        if (isBotfarm(guild)) {
            return;
        }

        if (event.getAuthor().isFake() ||
            event.getAuthor().isBot() ||
            event.isWebhookMessage() ||
            event.getMember() == null // Just in case Discord fucks up *again*
        ) {
            return;
        }

        final String rw = event.getMessage().getContentRaw();

        if (rw.equals(Settings.PREFIX + "shutdown")
            && isDev(event.getAuthor().getIdLong())) {
            logger.info("Initialising shutdown!!!");

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
                handleMessageEventChecked(rw, guild, event);
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

    private void handleMessageEventChecked(String rw, Guild guild, GuildMessageReceivedEvent event) {
        final User selfUser = event.getJDA().getSelfUser();
        final String selfRegex = "^<@!?" + selfUser.getId() + '>';
        final GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);
        final String customPrefix = settings.getCustomPrefix();

        if (!commandManager.isCommand(customPrefix, rw) && doAutoModChecks(event, settings, rw)) {
            return;
        }

        if (rw.matches(selfRegex + '$')) {
            sendMsg(event, String.format("Hey %s, try `%shelp` for a list of commands. If it doesn't work scream at _duncte123#1245_",
                event.getAuthor(),
                customPrefix)
            );
            return;
        }

        final String[] split = rw.replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+");
        final List<CustomCommand> autoResponses = commandManager.getAutoResponses(guild.getIdLong());

        if (!autoResponses.isEmpty() && invokeAutoResponse(autoResponses, split, event)) {
            return;
        }

        if (doesNotStartWithPrefix(event, customPrefix) || !canRunCommands(rw, customPrefix, event)) {
            return;
        }

        if (rw.matches(selfRegex + "(.*)")) {
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

    private boolean doesNotStartWithPrefix(GuildMessageReceivedEvent event, String customPrefix) {
        final String rwLower = event.getMessage().getContentRaw().toLowerCase();
        final String selfMember = event.getGuild().getSelfMember().getAsMention();
        final String selfUser = event.getJDA().getSelfUser().getAsMention();

        if (rwLower.startsWith(Settings.OTHER_PREFIX.toLowerCase())) {
            return false;
        }

        if (rwLower.startsWith(Settings.PREFIX.toLowerCase())) {
            return false;
        }

        if (rwLower.startsWith(customPrefix)) {
            return false;
        }

        if (rwLower.startsWith(selfMember)) {
            return false;
        }

        return !rwLower.startsWith(selfUser);
    }

    private boolean shouldBlockCommand(@Nonnull String customPrefix, @Nonnull String rw, @Nonnull String s) {
        return s.equalsIgnoreCase(
            rw.replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(customPrefix), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase()
        );
    }

    //                                    raw,    category?
    private boolean hasCorrectCategory(@Nonnull String rw, @Nonnull String categoryName, @Nonnull String customPrefix) {

        final ICommand command = commandManager.getCommand(
            rw.replaceFirst(Pattern.quote(customPrefix), Settings.PREFIX)
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

    private boolean canRunCommands(String rw, String customPrefix, @Nonnull GuildMessageReceivedEvent event) {

        final String topic = event.getChannel().getTopic();

        if (topic == null || topic.isEmpty()) {
            return true;
        }

        if (topicContains(event.getChannel(), "-commands")) {
            return false;
        }

        final String[] blocked = topic.split("-");

        for (String s : blocked) {
            if (s.startsWith("!")) {
                s = s.split("!")[1];

                if (isCategory(s.toUpperCase()) && !hasCorrectCategory(rw, s, customPrefix)) {
                    return false;
                }

                return !shouldBlockCommand(customPrefix, rw, s);
            }

            if (isCategory(s.toUpperCase()) && hasCorrectCategory(rw, s, customPrefix)) {
                return false;
            }

            if (shouldBlockCommand(customPrefix, rw, s)) {
                return false;
            }

        }

        return true;
    }

    /// <editor-fold desc="auto moderation" defaultstate="collapsed">
    private void checkMessageForInvites(Guild guild, GuildMessageReceivedEvent event, GuildSettings settings, String rw) {
        if (settings.isFilterInvites() && guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            final Matcher matcher = Message.INVITE_PATTERN.matcher(rw);
            if (matcher.find()) {
                //Get the invite Id from the message
                final String inviteID = matcher.group(matcher.groupCount());

                //Prohibiting failure because the bot is currently banned from the other guild.
                guild.retrieveInvites().queue((invites) -> {
                    //Check if the invite is for this guild, if it is not delete the message
                    if (invites.stream().noneMatch((invite) -> invite.getCode().equals(inviteID))) {
                        event.getMessage().delete().reason("Contained unauthorized invite.").queue((it) ->
                                sendMsg(event, event.getAuthor().getAsMention() +
                                    ", please don't post invite links here.", m -> m.delete().queueAfter(4, TimeUnit.SECONDS)),
                            (t) -> {}
                        );
                    }
                });
            }
        }
    }

    private boolean checkSwearFilter(Message messageToCheck, GenericGuildMessageEvent event, DunctebotGuild guild) {
        final GuildSettings settings = guild.getSettings();

        if (settings.isEnableSwearFilter() && !topicContains(event.getChannel(), PROFANITY_FILTER_DISABLE_FLAG)) {
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

            sendMsg(event.getChannel(),
                String.format("Hello there, %s please do not use cursive language within this Discord.",
                    messageToCheck.getAuthor().getAsMention()
                ),
                (m) -> m.delete().queueAfter(5, TimeUnit.SECONDS, null, (t) -> {}));

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

        final String rw = messageToCheck.getContentRaw().toLowerCase();

        for (final String foundWord : blacklist) {
            if (Pattern.compile("\\b" + foundWord + "\\b").matcher(rw).find()) {
                messageToCheck.delete()
                    .reason(String.format("Contains blacklisted word: \"%s\"", foundWord)).queue();

                modLog(String.format(
                    "Deleted message from %#s in %s for containing the blacklisted word \"%s\"",
                    messageToCheck.getAuthor(),
                    messageToCheck.getChannel(),
                    foundWord
                ), dbG);

                sendMsgFormatAndDeleteAfter(
                    (TextChannel) messageToCheck.getChannel(),
                    5,
                    TimeUnit.SECONDS,
                    "%s the word \"%s\" is blacklisted on this server",
                    messageToCheck.getMember(),
                    foundWord
                );

                return true;
            }
        }

        return false;
    }

    private void checkSpamFilter(Message messageToCheck, GuildMessageReceivedEvent event, GuildSettings settings, DunctebotGuild g) {
        if (settings.isEnableSpamFilter()) {
            final long[] rates = settings.getRatelimits();

            spamFilter.applyRates(rates);

            if (spamFilter.check(new Triple<>(event.getMember(), messageToCheck, settings.getKickState()))) {
                modLog(event.getJDA().getSelfUser(), event.getAuthor(),
                    settings.getKickState() ? "kicked" : "muted", "spam", g);
            }
        }
    }

    private boolean doAutoModChecks(@Nonnull GuildMessageReceivedEvent event, GuildSettings settings, String rw) {
        final Guild guild = event.getGuild();
        if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)
            && !Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {

            checkMessageForInvites(guild, event, settings, rw);

            final Message messageToCheck = event.getMessage();
            final DunctebotGuild dbG = new DunctebotGuild(event.getGuild(), variables);

            if (checkSwearFilter(messageToCheck, event, dbG)) {
                return true;
            }

            if (blacklistedWordCheck(dbG, messageToCheck, event.getMember(), settings.getBlacklistedWords())) {
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
            manager.getShardCache().forEach((jda) -> jda.setEventManager(null));

            try {
                // Sleep for 3 seconds
                Thread.sleep(TimeUnit.SECONDS.toMillis(3));
            }
            catch (InterruptedException ignored) {
            }

            // Kill all threads
            this.systemPool.shutdown();

            final WebRouter router = SkyBot.getInstance().getWebRouter();

            if (router != null) {
                router.shutdown();
            }

            AirUtils.stop(variables.getAudioUtils(), manager);
            BotCommons.shutdown(manager);

            // There are *some* applications (weeb.java *cough*) that are stupid
            // and do not allow us to shut down okhttp or create deamon threads
            if (kill) {
                System.exit(0);
            }
        }, "shutdown-thread");
        shutdownThread.start();
    }
}
