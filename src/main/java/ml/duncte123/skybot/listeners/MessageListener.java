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
import ml.duncte123.skybot.utils.BadWordFilter;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import ml.duncte123.skybot.utils.SpamFilter;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class MessageListener extends BaseListener {

    protected final CommandManager commandManager = variables.getCommandManager();
    private final BadWordFilter wordFilter = new BadWordFilter();
    protected final SpamFilter spamFilter = new SpamFilter();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        final Guild guild = event.getGuild();

        if (isBotfarm(guild)) {
            return;
        }

        //We only want to respond to members/users
        if (event.getAuthor().isFake() || event.getAuthor().isBot() || event.getMember() == null) {
            return;
        }

        final String rw = event.getMessage().getContentRaw();

        if (rw.equals(Settings.PREFIX + "shutdown")
            && Settings.developers.contains(event.getAuthor().getIdLong())) {
            logger.info("Initialising shutdown!!!");
            shuttingDown = true;

            final ShardManager manager = event.getJDA().asBot().getShardManager();

            event.getMessage().addReaction("✅").queue(
                success -> killAllShards(manager),
                failure -> killAllShards(manager)
            );

            return;
        }

        final String selfMember = guild.getSelfMember().getAsMention();
        final String selfUser = event.getJDA().getSelfUser().getAsMention();
        final GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);
        final String rwLower = rw.toLowerCase();

        if (doAutoModChecks(event, settings, rw)) {
            return;
        }

        if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser()) && rw.equals(selfMember)) {
            sendMsg(event, String.format("Hey <@%s>, try `%shelp` for a list of commands. If it doesn't work scream at _duncte123#1245_",
                event.getAuthor().getId(),
                Settings.PREFIX)
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

                commandManager.dispatchCommand(cmd, "",  Arrays.asList(split).subList(1, split.length), event);
                return;
            }

        }

        if (!rwLower.startsWith(Settings.PREFIX.toLowerCase()) &&
            !rw.startsWith(settings.getCustomPrefix())
            && !rw.startsWith(selfMember)
            && !rw.startsWith(selfUser)
            && !rwLower.startsWith(Settings.OTHER_PREFIX.toLowerCase())) {
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

    private boolean shouldBlockCommand(@NotNull GuildSettings settings, @NotNull String rw, @NotNull String s) {
        return s.equalsIgnoreCase(
            rw.replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(settings.getCustomPrefix()), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase()
        );
    }

    //                                    raw,    category?
    private boolean hasCorrectCategory(@NotNull String rw, @NotNull String categoryName) {

        final ICommand command = commandManager.getCommand(rw.replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Settings.PREFIX)
            .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase());

        if (command == null) {
            return false;
        }

        return command.getCategory() == CommandCategory.valueOf(categoryName.toUpperCase());
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isCategory(@NotNull String name) {
        try {
            return CommandCategory.valueOf(name.toUpperCase()) != null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private boolean canRunCommands(String rw, GuildSettings settings, @NotNull GuildMessageReceivedEvent event) {

        final String topic = event.getChannel().getTopic();

        if (topic == null || topic.isEmpty()) {
            return true;
        }

        if (topic.contains("-commands"))
            return false;

        final String[] blocked = topic.split("-");

        for (String s : blocked) {
            if (s.startsWith("!")) {
                s = s.split("!")[1];

                if (isCategory(s.toUpperCase()) && !hasCorrectCategory(rw, s)) {
                    return false;
                }

                /*if (shouldBlockCommand(settings, rw, s))
                    return false;

                return true;*/
                return !shouldBlockCommand(settings, rw, s);
            }

            if (isCategory(s.toUpperCase()) && hasCorrectCategory(rw, s)) {
                return false;
            }

            if (shouldBlockCommand(settings, rw, s)) {
                return false;
            }

        }

        return true;
    }

    private boolean doAutoModChecks(@NotNull GuildMessageReceivedEvent event, GuildSettings settings, String rw) {
        final Guild guild = event.getGuild();
        if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)
            && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {

            if (settings.isFilterInvites() && guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                final Matcher matcher = Message.INVITE_PATTERN.matcher(rw);
                if (matcher.find()) {
                    //Get the invite Id from the message
                    final String inviteID = matcher.group(matcher.groupCount());

                    //Prohibiting failure because the bot is currently banned from the other guild.
                    guild.getInvites().queue((invites) -> {
                        //Check if the invite is for this guild, if it is not delete the message
                        if (invites.stream().noneMatch((invite) -> invite.getCode().equals(inviteID))) {
                            event.getMessage().delete().reason("Contained unauthorized invite.").queue(it ->
                                sendMsg(event, event.getAuthor().getAsMention() +
                                    ", please don't post invite links here.", m -> m.delete().queueAfter(4, TimeUnit.SECONDS))
                            );
                        }
                    });
                }
            }

            if (settings.isEnableSwearFilter()) {
                final Message messageToCheck = event.getMessage();
                if (wordFilter.filterText(rw)) {
                    messageToCheck.delete().reason("Blocked for bad swearing: " + messageToCheck.getContentDisplay())
                        .queue(null, (t) -> {
                        });

                    sendMsg(event,
                        String.format("Hello there, %s please do not use cursive language within this Discord.",
                            event.getAuthor().getAsMention()
                        ),
                        m -> m.delete().queueAfter(5, TimeUnit.SECONDS, null, (t) -> {
                        }));
                    return true;
                }
            }

            if (settings.isEnableSpamFilter()) {
                final Message messageToCheck = event.getMessage();
                final long[] rates = settings.getRatelimits();
                spamFilter.applyRates(rates);
                final DunctebotGuild g = new DunctebotGuild(guild);
                if (spamFilter.check(new Triple<>(event.getMember(), messageToCheck, settings.getKickState()))) {
                    ModerationUtils.modLog(event.getJDA().getSelfUser(), event.getAuthor(),
                        settings.getKickState() ? "kicked" : "muted", "spam", g);
                }
            }
        }

        return false;
    }

    private void killAllShards(@NotNull ShardManager manager) {
        manager.shutdown();
        /*manager.getShards().forEach(jda -> {
            logger.info(String.format("Shard %s has been shut down", jda.getShardInfo().getShardId()));
            jda.shutdown();
        });*/
    }
}
