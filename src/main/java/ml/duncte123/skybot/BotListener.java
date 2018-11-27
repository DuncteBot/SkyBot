/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import fredboat.audio.player.LavalinkManager;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import kotlin.Triple;
import me.duncte123.botcommons.text.TextColor;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.*;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.*;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.*;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public class BotListener extends ListenerAdapter {

    /**
     * Check if we are updating
     */
    public static boolean isUpdating = false;
    private final Logger logger = LoggerFactory.getLogger(BotListener.class);
    /**
     * This filter helps us to fiter out swearing
     */
    private final BadWordFilter wordFilter = new BadWordFilter();
    /**
     * This filter helps us to fiter out spam
     */
    private final SpamFilter spamFilter;
    /**
     * This timer is for checking unbans
     * This timer is for clearing our caches
     */
    private final ScheduledExecutorService systemPool = Executors.newScheduledThreadPool(3,
        r -> new Thread(r, "Bot-Service-Thread"));
    /**
     * A custom consumer that cancels the stupid unknown message error
     */
    private final Consumer<Throwable> CUSTOM_QUEUE_ERROR = it -> {
        if (it instanceof ErrorResponseException && ((ErrorResponseException) it).getErrorCode() != 10008) {
            logger.error("RestAction queue returned failure", it);
        }
    };

    private final DBManager database;
    private final CommandManager commandManager;
    private final Variables variables;
    // A list of servers that list bots
    private final TLongList botLists = new TLongArrayList(
        new long[]{
            110373943822540800L, // Dbots
            264445053596991498L, // Dbl
            374071874222686211L, // Bots for discord
            112319935652298752L, // Carbon
            439866052684283905L, // Discord Boats
            387812458661937152L, // Botlist.space
            483344253963993113L, // AutomaCord
            454933217666007052L, // Divine Discord Bot List
            446682534135201793L, // Discords best bots
            477792727577395210L, // discordbotlist.xyz
            475571221946171393L, // bots.discordlist.app
        }
    );
    /**
     * Make sure that we don't exit when we don't want to
     */
    private boolean shuttingDown = false;
    /**
     * This tells us if the {@link #systemPool} is running
     */
    private boolean unbanTimerRunning = false;
    /**
     * Tells us whether {@link #systemPool} clears cache of our {@link #spamFilter}.
     */
    private boolean isCacheCleanerActive = false;
    private short shardsReady = 0;

    BotListener(Variables variables) {
        this.variables = variables;
        this.database = variables.getDatabase();
        this.commandManager = variables.getCommandManager();

        this.spamFilter = new SpamFilter(database, variables);
    }

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Logged in as {} (Shard {})", String.format("%#s", event.getJDA().getSelfUser()), event.getJDA().getShardInfo().getShardId());

        //Start the timers if they have not been started yet
        if (!unbanTimerRunning/* && Variables.NONE_SQLITE*/) {
            logger.info("Starting the unban timer.");
            //Register the timer for the auto unbans
            systemPool.scheduleAtFixedRate(() -> ModerationUtils.checkUnbans(variables), 5, 5, TimeUnit.MINUTES);
            unbanTimerRunning = true;
        }

        if (!isCacheCleanerActive) {
            logger.info("Starting spam-cache-cleaner!");
            systemPool.scheduleAtFixedRate(spamFilter::clearMessages, 20, 13, TimeUnit.SECONDS);
            isCacheCleanerActive = true;
        }

        shardsReady++;
        ShardManager manager = event.getJDA().asBot().getShardManager();
        if (shardsReady == manager.getShardsTotal()) {

            loadPatrons(manager);
        }
    }

    private void loadPatrons(@NotNull ShardManager manager) {
        logger.info("Collecting patrons");

        Guild supportGuild = manager.getGuildById(Command.supportGuildId);

        List<Long> patronsList = supportGuild.getMembersWithRoles(supportGuild.getRoleById(Command.patronsRole))
            .stream().map(Member::getUser).map(User::getIdLong).collect(Collectors.toList());

        Command.patrons.addAll(patronsList);

        logger.info("Found {} normal patrons", Command.patrons.size());

        List<User> guildPatronsList = supportGuild.getMembersWithRoles(supportGuild.getRoleById(Command.guildPatronsRole))
            .stream().map(Member::getUser).collect(Collectors.toList());

        TLongList patronGuildsTrove = new TLongArrayList();

        guildPatronsList.forEach((patron) -> {
            List<Long> guilds = manager.getMutualGuilds(patron).stream()
                .filter((it) -> it.getOwner().equals(it.getMember(patron)) ||
                    it.getMember(patron).hasPermission(Permission.ADMINISTRATOR))
                .map(Guild::getIdLong)
                .collect(Collectors.toList());

            patronGuildsTrove.addAll(guilds);
        });

        Command.guildPatrons.addAll(patronGuildsTrove);

        logger.info("Found {} guild patrons", patronGuildsTrove.size());

        GuildUtils.reloadOneGuildPatrons(manager, database);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        Guild guild = event.getGuild();

        if (isBotfarm(guild)) {
            return;
        }

        //We only want to respond to members/users
        if (event.getAuthor().isFake() || event.getAuthor().isBot() || event.getMember() == null) {
            return;
        }

        if (event.getMessage().getContentRaw().equals(Settings.PREFIX + "shutdown")
            && Settings.developers.contains(event.getAuthor().getIdLong())) {
            logger.info("Initialising shutdown!!!");
            shuttingDown = true;

            event.getMessage().addReaction("✅").queue(
                success -> killAllShards(event.getJDA().asBot().getShardManager()),
                failure -> killAllShards(event.getJDA().asBot().getShardManager())
            );

            return;
        }

        String selfMember = guild.getSelfMember().getAsMention();
        String selfUser = event.getJDA().getSelfUser().getAsMention();
        GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);
        String rw = event.getMessage().getContentRaw();
        String rwLower = rw.toLowerCase();

        if (doAutoModChecks(event, settings, rw)) return;

        if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())
            && rw.equals(selfMember)) {
            sendMsg(event, String.format("Hey <@%s>, try `%shelp` for a list of commands. If it doesn't work scream at _duncte123#1245_",
                event.getAuthor().getId(),
                Settings.PREFIX)
            );
            return;
        }

        if (!rwLower.startsWith(Settings.PREFIX.toLowerCase()) &&
            !rw.startsWith(settings.getCustomPrefix())
            && !rw.startsWith(selfMember)
            && !rw.startsWith(selfUser)
            && !rwLower.startsWith(Settings.OTHER_PREFIX.toLowerCase())) {
            return;
        }

        if (!canRunCommands(rw, settings, event)) return;

        if (!rw.startsWith(selfMember) && !rw.startsWith(selfUser)) {
            //Handle the command
            commandManager.runCommand(event);
            return;
        }

        final String[] split = rw.replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+");
        //Handle the chat command
        ICommand cmd = commandManager.getCommand("chat");
        if (cmd != null)
            cmd.executeCommand(new CommandContext(
                "chat",
                Arrays.asList(split).subList(1, split.length),
                event,
                variables
            ));
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        if (event.getMember().equals(guild.getSelfMember())) return;

        /*
        {{USER_MENTION}} = mention user
        {{USER_NAME}} = return username
        {{GUILD_NAME}} = the name of the guild
        {{GUILD_USER_COUNT}} = member count
        {{GUILD_OWNER_MENTION}} = mention the guild owner
        {{GUILD_OWNER_NAME}} = return the name form the owner
         */

        GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);

        if (settings.isEnableJoinMessage()) {
            long welcomeLeaveChannelId = (settings.getWelcomeLeaveChannel() <= 0)
                ? GuildUtils.getPublicChannel(guild).getIdLong() : settings.getWelcomeLeaveChannel();

            TextChannel welcomeLeaveChannel = guild.getTextChannelById(welcomeLeaveChannelId);
            String msg = parseGuildVars(settings.getCustomJoinMessage(), event);

            if (!msg.isEmpty() || "".equals(msg) || welcomeLeaveChannel != null)
                sendMsg(welcomeLeaveChannel, msg);
        }

        if (settings.isAutoroleEnabled() && guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            Role r = guild.getRoleById(settings.getAutoroleRole());

            if (r != null && !guild.getPublicRole().equals(r) && guild.getSelfMember().canInteract(r)) {
                guild.getController()
                    .addSingleRoleToMember(event.getMember(), r).queue(null, it -> {
                });
            }
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        Guild guild = event.getGuild();

        if (guild.getIdLong() == Command.supportGuildId) {
            handlePatronRemoval(event.getUser().getIdLong());
        }

        if (event.getMember().equals(guild.getSelfMember())) return;
        GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);

        if (settings.isEnableJoinMessage()) {
            long welcomeLeaveChannelId = (settings.getWelcomeLeaveChannel() <= 0)
                ? GuildUtils.getPublicChannel(guild).getIdLong() : settings.getWelcomeLeaveChannel();

            TextChannel welcomeLeaveChannel = guild.getTextChannelById(welcomeLeaveChannelId);
            String msg = parseGuildVars(settings.getCustomLeaveMessage(), event);

            if (!msg.isEmpty() || "".equals(msg) || welcomeLeaveChannel != null) {
                sendMsg(welcomeLeaveChannel, msg);
            }
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();

        if (isBotfarm(guild)) {
            return;
        }

        logger.info("{}Joining guild {}, ID: {} on shard {}{}",
            TextColor.GREEN,
            guild.getName(),
            guild.getId(),
            guild.getJDA().getShardInfo().getShardId(),
            TextColor.RESET
        );

        GuildSettingsUtils.registerNewGuild(guild, variables);
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();

        logger.info("{}Leaving guild: {} ({}).{}",
            TextColor.RED,
            guild.getName(),
            guild.getId(),
            TextColor.RESET
        );
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Guild guild = event.getGuild();
        LavalinkManager manager = LavalinkManager.ins;
        GuildVoiceState voiceState = event.getVoiceState();

        if (manager.isConnected(guild)
            && !voiceState.getMember().equals(guild.getSelfMember())) {
            VoiceChannel vc = manager.getConnectedChannel(guild);

            if (vc != null) {

                if (!event.getChannelLeft().equals(vc)) {
                    return;
                }

                channelCheckThing(guild, event.getChannelLeft());
            }
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        Guild guild = event.getGuild();
        LavalinkManager manager = LavalinkManager.ins;

        if (manager.isConnected(guild)) {
            VoiceChannel connected = manager.getConnectedChannel(guild);

            if (connected == null) {
                return;
            }

            if (!event.getChannelJoined().equals(connected) && event.getMember().equals(guild.getSelfMember())) {
                channelCheckThing(guild, connected);

                return;
            }

            if (event.getChannelLeft().equals(connected)) {
                channelCheckThing(guild, event.getChannelLeft());
                //return;
            }
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {

        if (event.getGuild().getIdLong() != Command.supportGuildId) {
            return;
        }

        for (Role role : event.getRoles()) {
            long roleId = role.getIdLong();

            if (!(roleId == Command.patronsRole || roleId == Command.guildPatronsRole || roleId == Command.oneGuildPatronsRole)) {
                continue;
            }

            handlePatronRemoval(event.getUser().getIdLong());
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {

        if (event.getGuild().getIdLong() != Command.supportGuildId) {
            return;
        }

        User user = event.getUser();
        long userId = user.getIdLong();
        ShardManager manager = event.getJDA().asBot().getShardManager();

        for (Role role : event.getRoles()) {
            long roleId = role.getIdLong();

            if (roleId == Command.patronsRole) {
                Command.patrons.add(userId);
            }

            if (roleId == Command.guildPatronsRole) {
                List<Long> guilds = manager.getMutualGuilds(user).stream()
                    .filter((it) -> {
                        Member member = it.getMember(user);

                        return it.getOwner().equals(member) || member.hasPermission(Permission.ADMINISTRATOR);
                    })
                    .map(Guild::getIdLong)
                    .collect(Collectors.toList());

                Command.guildPatrons.addAll(guilds);
            }

            if (roleId == Command.oneGuildPatronsRole) {
                handleNewOneGuildPatron(userId);
            }
        }

    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        if (!shuttingDown) return;

        MusicCommand.shutdown();

        //Kill other things
        //((EvalCommand) AirUtils.COMMAND_MANAGER.getCommand("eval")).shutdown();
        if (unbanTimerRunning && isCacheCleanerActive)
            this.systemPool.shutdown();

        AirUtils.stop(database, variables.getAudioUtils());
        commandManager.commandThread.shutdown();

        /*
         * Only shut down if we are not updating
         */
        if (!isUpdating) {
            System.exit(0);
        }
    }

    private boolean isBotfarm(Guild guild) {

        if (botLists.contains(guild.getIdLong())) {
            return false;
        }

        // How many members should we at least have in the server
        // before starting to conciser it as a botfarm
        int minTotalMembers = 30;
        // What percentage of bots do we allow
        double maxBotPercentage = 70;

        double[] botToUserRatio = GuildUtils.getBotRatio(guild);
        long[] counts = GuildUtils.getBotAndUserCount(guild);
        long totalMembers = guild.getMemberCache().size();

        // if (!(botToUserRatio[1] >= maxBotPercentage && totalMembers > 30))
        logger.debug("totalMembers > minTotalMembers " + (totalMembers > minTotalMembers));
        logger.debug("botToUserRatio[1] <= maxBotPercentage " + (botToUserRatio[1] <= maxBotPercentage));
        if (!(botToUserRatio[1] >= maxBotPercentage && totalMembers > minTotalMembers)) {
            return false;
        }

        sendMsg(GuildUtils.getPublicChannel(guild),
            String.format("Hello %s, this server is now blacklisted as botfarm and the bot will leave the guild (%s humans / %s bots).",
                guild.getOwner().getAsMention(),
                counts[0],
                counts[1]
            ),
            message -> guild.leave().queue(),
            er -> guild.leave().queue()
        );

        logger.info("{}Botfarm found: {} {}% bots ({} humans / {} bots){}",
            TextColor.RED,
            guild,
            botToUserRatio[1],
            counts[0],
            counts[1],
            TextColor.RESET
        );

        return true;
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

        ICommand command = commandManager.getCommand(rw.replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Settings.PREFIX)
            .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase());

        if (command == null)
            return false;

        return command.getCategory() == CommandCategory.valueOf(categoryName.toUpperCase());
    }

    /**
     * This handles the guild leave/ join events to deferments if the channel is empty
     *
     * @param g
     *         the guild
     * @param vc
     *         the voice channel
     */
    private void channelCheckThing(Guild g, @NotNull VoiceChannel vc) {

        if (vc.getMembers().stream().filter(m -> !m.getUser().isBot()).count() < 1) {
            GuildMusicManager manager = variables.getAudioUtils().getMusicManager(g);
            manager.player.stopTrack();
            manager.player.setPaused(false);
            manager.scheduler.queue.clear();
            MusicCommand.cooldowns.put(g.getIdLong(), 12600);

            if (g.getAudioManager().getConnectionListener() != null)
                g.getAudioManager().setConnectionListener(null);

            sendMsg(g.getTextChannelById(manager.latestChannel), "Leaving voice channel because all the members have left it.");

            if (LavalinkManager.ins.isConnected(g)) {
                LavalinkManager.ins.closeConnection(g);
                variables.getAudioUtils().getMusicManagers().remove(g.getIdLong());
            }
        }
    }

    @NotNull
    private String parseGuildVars(String rawMessage, GenericGuildMemberEvent event) {

        if (!(event instanceof GuildMemberJoinEvent) && !(event instanceof GuildMemberLeaveEvent))
            return "NOPE";

        Guild guild = event.getGuild();
        GuildSettings s = GuildSettingsUtils.getGuild(guild, variables);
        long welcomeLeaveChannel = s.getWelcomeLeaveChannel();
        long autoRoleId = s.getAutoroleRole();

        String message = CustomCommandUtils.PARSER.clear()
            .put("user", event.getUser())
            .put("guild", event.getGuild())
            .put("channel", event.getGuild().getTextChannelById(welcomeLeaveChannel))
            .put("args", "")
            .parse(rawMessage);

        return message.replaceAll("\\{\\{USER_MENTION}}", event.getUser().getAsMention())
            .replaceAll("\\{\\{USER_NAME}}", event.getUser().getName())
            .replaceAll("\\{\\{USER_FULL}}", String.format("%#s", event.getUser()))
            .replaceAll("\\{\\{IS_USER_BOT}}", String.valueOf(event.getUser().isBot()))
            .replaceAll("\\{\\{GUILD_NAME}}", guild.getName())
            .replaceAll("\\{\\{GUILD_USER_COUNT}}", guild.getMemberCache().size() + "")

            //This one can be kept a secret :P
            .replaceAll("\\{\\{AUTO_ROLE_NAME}", autoRoleId <= 0 ?
                "Not set" : guild.getRoleById(autoRoleId).getName())
            .replaceAll("\\{\\{EVENT_TYPE}}", event instanceof GuildMemberJoinEvent ? "joined" : "left");
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isCategory(@NotNull String name) {
        try {
            return CommandCategory.valueOf(name.toUpperCase()) != null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private void killAllShards(@NotNull ShardManager manager) {
        manager.shutdown();
        /*manager.getShards().forEach(jda -> {
            logger.info(String.format("Shard %s has been shut down", jda.getShardInfo().getShardId()));
            jda.shutdown();
        });*/
    }

    private boolean canRunCommands(String rw, GuildSettings settings, @NotNull GuildMessageReceivedEvent event) {

        String topic = event.getChannel().getTopic();

        if (topic == null || topic.isEmpty()) {
            return true;
        }

        if (topic.contains("-commands"))
            return false;

        String[] blocked = topic.split("-");

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
        Guild guild = event.getGuild();
        if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)
            && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {

            if (settings.isFilterInvites() && guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                Matcher matcher = Message.INVITE_PATTERN.matcher(rw);
                if (matcher.find()) {
                    //Get the invite Id from the message
                    String inviteID = matcher.group(matcher.groupCount());

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
                Message messageToCheck = event.getMessage();
                if (wordFilter.filterText(rw)) {
                    messageToCheck.delete().reason("Blocked for bad swearing: " + messageToCheck.getContentDisplay())
                        .queue(null, CUSTOM_QUEUE_ERROR);

                    sendMsg(event,
                        String.format("Hello there, %s please do not use cursive language within this Discord.",
                            event.getAuthor().getAsMention()
                        ),
                        m -> m.delete().queueAfter(5, TimeUnit.SECONDS, null, CUSTOM_QUEUE_ERROR));
                    return true;
                }
            }

            if (settings.isEnableSpamFilter()) {
                Message messageToCheck = event.getMessage();
                long[] rates = settings.getRatelimits();
                spamFilter.applyRates(rates);
                DunctebotGuild g = new DunctebotGuild(guild, variables);
                if (spamFilter.check(new Triple<>(event.getMember(), messageToCheck, settings.getKickState()))) {
                    ModerationUtils.modLog(event.getJDA().getSelfUser(), event.getAuthor(),
                        settings.getKickState() ? "kicked" : "muted", "spam", g);
                }
            }
        }

        return false;
    }

    private void handlePatronRemoval(long userId) {
        // Remove the user from the patrons list
        Command.patrons.remove(userId);

        // Remove the user from the one guild patrons
        Command.oneGuildPatrons.remove(userId);
        GuildUtils.removeOneGuildPatron(userId, database);

        // TODO: Handle full guild case
        // But hey, who cares right now
    }

    private void handleNewOneGuildPatron(long userId) {
        database.run(() -> {

            try (Connection connection = database.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM " + database.getName() + ".oneGuildPatrons WHERE user_id = ? LIMIT 1");

                statement.setLong(1, userId);

                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    long guildId = Long.parseLong(resultSet.getString("guild_id"));

                    Command.oneGuildPatrons.put(userId, guildId);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
    }
}
