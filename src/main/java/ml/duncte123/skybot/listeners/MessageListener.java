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
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.PerspectiveApi;
import ml.duncte123.skybot.utils.SpamFilter;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormatAndDeleteAfter;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

public class MessageListener extends BaseListener {

    protected final CommandManager commandManager = variables.getCommandManager();
    final SpamFilter spamFilter = new SpamFilter(variables);

    MessageListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        variables.getDatabase().run(() -> {
            final DunctebotGuild guild = new DunctebotGuild(event.getGuild(), variables);

            if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) &&
                !event.getMember().hasPermission(Permission.MESSAGE_MANAGE) &&
                guild.getSettings().isEnableSwearFilter()) {
                checkSwearFilter(event.getMessage(), event, guild);
            }
        });
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        final Guild guild = event.getGuild();

        if (isBotfarm(guild)) {
            return;
        }

        //We only want to respond to members/users
        if (event.getAuthor().isFake() || event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        final String rw = event.getMessage().getContentRaw();

        if (rw.equals(Settings.PREFIX + "shutdown")
            && Settings.developers.contains(event.getAuthor().getIdLong())) {
            logger.info("Initialising shutdown!!!");
            shuttingDown = true;

            commandManager.shutdown();

            final ShardManager manager = event.getJDA().asBot().getShardManager();

            event.getMessage().addReaction("a:_yes:577795293546938369").queue(
                success -> killAllShards(manager),
                failure -> killAllShards(manager)
            );

            return;
        }

        variables.getDatabase().run(() -> {
            try {
                final String selfMember = guild.getSelfMember().getAsMention();
                final String selfUser = event.getJDA().getSelfUser().getAsMention();
                final GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);

                if (!commandManager.isCommand(settings.getCustomPrefix(), rw) && doAutoModChecks(event, settings, rw)) {
                    return;
                }

                if (rw.equals(selfMember) || rw.equals(selfUser)) {
                    sendMsg(event, String.format("Hey %s, try `%shelp` for a list of commands. If it doesn't work scream at _duncte123#1245_",
                        event.getAuthor(),
                        settings.getCustomPrefix())
                    );
                    return;
                }

                final String[] split = rw.replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+");
                final List<CustomCommand> autoResponses = commandManager.getAutoResponses(guild.getIdLong());

                if (!autoResponses.isEmpty()) {
                    final String stripped = event.getMessage().getContentStripped().toLowerCase();

                    final Optional<CustomCommand> match = autoResponses.stream()
                        .filter((cmd) -> stripped.contains(cmd.getName().toLowerCase())).findFirst();

                    if (match.isPresent()) {
                        final CustomCommand cmd = match.get();

                        commandManager.dispatchCommand(cmd, "", Arrays.asList(split).subList(1, split.length), event);
                        return;
                    }

                }

                if (doesNotStartWithPrefix(event, settings)) {
                    return;
                }

                if (!canRunCommands(rw, settings, event)) {
                    return;
                }

                if (!rw.startsWith(selfMember) && !rw.startsWith(selfUser)) {
                    //Handle the command
                    commandManager.runCommand(event);
                    return;
                }
                //Handle the chat command
                final ICommand cmd = commandManager.getCommand("chat");

                if (cmd != null) {
                    cmd.executeCommand(new CommandContext(
                        "chat",
                        Arrays.asList(split).subList(1, split.length),
                        event,
                        variables
                    ));
                }
            }
            catch (Exception e) {
                Sentry.capture(e);
                e.printStackTrace();
            }
        });
    }

    private boolean doesNotStartWithPrefix(GuildMessageReceivedEvent event, GuildSettings settings) {
        final String rwLower = event.getMessage().getContentRaw().toLowerCase();
        final String selfMember = event.getGuild().getSelfMember().getAsMention();
        final String selfUser = event.getJDA().getSelfUser().getAsMention();
        final String customPrefix = settings.getCustomPrefix();

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

    private boolean shouldBlockCommand(@Nonnull GuildSettings settings, @Nonnull String rw, @Nonnull String s) {
        return s.equalsIgnoreCase(
            rw.replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(settings.getCustomPrefix()), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase()
        );
    }

    //                                    raw,    category?
    private boolean hasCorrectCategory(@Nonnull String rw, @Nonnull String categoryName, @Nonnull GuildSettings settings) {

        final ICommand command = commandManager.getCommand(
            rw.replaceFirst(Pattern.quote(settings.getCustomPrefix()), Settings.PREFIX)
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

    private boolean canRunCommands(String rw, GuildSettings settings, @Nonnull GuildMessageReceivedEvent event) {

        final String topic = event.getChannel().getTopic();

        if (topic == null || topic.isEmpty()) {
            return true;
        }

        if (topic.contains("-commands")) {
            return false;
        }

        final String[] blocked = topic.split("-");

        for (String s : blocked) {
            if (s.startsWith("!")) {
                s = s.split("!")[1];

                if (isCategory(s.toUpperCase()) && !hasCorrectCategory(rw, s, settings)) {
                    return false;
                }

                return !shouldBlockCommand(settings, rw, s);
            }

            if (isCategory(s.toUpperCase()) && hasCorrectCategory(rw, s, settings)) {
                return false;
            }

            if (shouldBlockCommand(settings, rw, s)) {
                return false;
            }

        }

        return true;
    }

    private void checkMessageForInvites(Guild guild, GuildMessageReceivedEvent event, GuildSettings settings, String rw) {
        if (settings.isFilterInvites() && guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            final Matcher matcher = Message.INVITE_PATTERN.matcher(rw);
            if (matcher.find()) {
                //Get the invite Id from the message
                final String inviteID = matcher.group(matcher.groupCount());

                //Prohibiting failure because the bot is currently banned from the other guild.
                guild.getInvites().queue((invites) -> {
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

        if (settings.isEnableSwearFilter()) {
            final float score = PerspectiveApi.checkSevereToxicity(
                messageToCheck.getContentStripped(),
                event.getChannel().getId(),
                variables.getConfig().apis.googl,
                variables.getJackson());

            if (score < 0.7f) {
                return false;
            }

            final String display = messageToCheck.getContentDisplay();

            messageToCheck.delete().reason("Blocked for swearing: " + display)
                .queue(null, (t) -> {});

            sendMsg(event.getChannel(),
                String.format("Hello there, %s please do not use cursive language within this Discord.",
                    messageToCheck.getAuthor().getAsMention()
                ),
                (m) -> m.delete().queueAfter(5, TimeUnit.SECONDS, null, (t) -> {}));

            modLog(String.format(
                "Message by %#s deleted in %s for profanity, message content was:```\n%s```",
                messageToCheck.getAuthor(),
                event.getChannel().getAsMention(),
                display
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
            && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {

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

    private void killAllShards(@Nonnull ShardManager manager) {
        manager.shutdown();
        /*manager.getShards().forEach(jda -> {
            logger.info(String.format("Shard %s has been shut down", jda.getShardInfo().getShardId()));
            jda.shutdown();
        });*/
    }
}
