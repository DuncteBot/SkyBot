/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import kotlin.Triple;
import me.duncte123.botCommons.text.TextColor;
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
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.duncte123.botCommons.messaging.MessageUtils.sendMsg;

public class BotListener extends ListenerAdapter {

    private static final Pattern DISCORD_INVITE_PATTERN = Pattern.compile("(http|https)?(:)?(//)?(discordapp|discord).(gg|io|me|com)/(\\w+:?\\w*@)?(\\S+)(:[0-9]+)?(/|/([\\w#!:.?+=&%@!-/]))?");
    /**
     * Check if we are updating
     */
    public static boolean isUpdating = false;
    /**
     * Make sure that we don't exit when we don't want to
     */
    private boolean shuttingDown = false;
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
    /**
     * This is used to check if we should trigger a update for the guild count when we leave a guild
     */
    private final List<Long> botFamrs = new ArrayList<>();
    private final DBManager database;
    private final CommandManager commandManager;
    private final Variables variables;
    private final List<Long> botLists = List.of(
            110373943822540800L,
            264445053596991498L,
            374071874222686211L
    );
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

            logger.info("Collecting patrons");
            Guild supportGuild = manager.getGuildById(Command.supportGuildId);
            List<Long> patrons = supportGuild.getMembersWithRoles(supportGuild.getRoleById(Command.patronsRole))
                    .stream().map(Member::getUser).map(User::getIdLong).collect(Collectors.toList());
            Command.patrons.addAll(patrons);

            logger.info("Found {} normal patrons", patrons.size());

            List<User> guildPatrons = supportGuild.getMembersWithRoles(supportGuild.getRoleById(Command.guildPatronsRole))
                    .stream().map(Member::getUser).collect(Collectors.toList());

            List<Long> patronGuilds = new ArrayList<>();

            guildPatrons.forEach((patron) -> {
                List<Long> guilds = manager.getMutualGuilds(patron).stream()
                        .filter((it) -> it.getOwner().equals(it.getMember(patron)) ||
                                it.getMember(patron).hasPermission(Permission.ADMINISTRATOR))
                        .map(Guild::getIdLong)
                        .collect(Collectors.toList());

                patronGuilds.addAll(guilds);
            });
            Command.guildPatrons.addAll(patronGuilds);

            logger.info("Found {} guild patrons", patronGuilds.size());
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        //We only want to respond to members/users
        if (event.getAuthor().isFake() || event.getAuthor().isBot() || event.getMember() == null)
            return;
        //noinspection deprecation
        if (event.getMessage().getContentRaw().equals(Settings.PREFIX + "shutdown")
                && Settings.wbkxwkZPaG4ni5lm8laY.contains(event.getAuthor().getIdLong())) {
            logger.info("Initialising shutdown!!!");
            shuttingDown = true;

            event.getMessage().addReaction("✅").queue(
                    success -> killAllShards(event.getJDA().asBot().getShardManager()),
                    failure -> killAllShards(event.getJDA().asBot().getShardManager())
            );

            return;
        }

        Guild guild = event.getGuild();
        GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);
        String rw = event.getMessage().getContentRaw();

        if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)
                && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {

            if (settings.isFilterInvites() && guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                Matcher matcher = DISCORD_INVITE_PATTERN.matcher(rw);
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
                    }, (__) -> {
                    }/*, (thr) -> {
                    try {
                        throw new SkybotContextException(thr.getMessage(), thr);
                    } catch (SkybotContextException e) {
                        MessageUtils.sendMsg(event, "I can not read the guild invites due to a lack of permissions.\n" +
                                "Grant the permission `MANAGE_SERVER` for me.\n" +
                                "Error: " +  e.getMessage());
                    }
                }*/);
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
                            m -> m.delete().queueAfter(3, TimeUnit.SECONDS, null, CUSTOM_QUEUE_ERROR));
                    return;
                }
            }

            if (settings.getEnableSpamFilter()) {
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

        if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())
                && rw.equals(guild.getSelfMember().getAsMention())) {
            sendMsg(event, String.format("Hey <@%s>, try `%shelp` for a list of commands. If it doesn't work scream at _duncte123#1245_",
                    event.getAuthor().getId(),
                    Settings.PREFIX)
            );
            return;
        } else if (!rw.toLowerCase().startsWith(Settings.PREFIX.toLowerCase()) &&
                !rw.startsWith(settings.getCustomPrefix())
                && !rw.startsWith(guild.getSelfMember().getAsMention())
                && !rw.toLowerCase().startsWith(Settings.OTHER_PREFIX.toLowerCase())) {
            return;
        }

        //If the topic contains -commands ignore it
        if (event.getChannel().getTopic() != null) {
            String[] blocked = event.getChannel().getTopic().split("-");
            if (event.getChannel().getTopic().contains("-commands"))
                return;
            for (String s : blocked) {
                if (s.startsWith("!")) {
                    s = s.split("!")[1];
                    if (isCategory(s.toUpperCase())) {
                        if (!shouldBlockCommand(rw, s)) {
                            return;
                        }
                    } else {
                        if (!startsWithPrefix(settings, rw, s))
                            return;
                    }
                } else {
                    if (isCategory(s.toUpperCase())) {
                        if (shouldBlockCommand(rw, s)) {
                            return;
                        }
                    } else {
                        if (startsWithPrefix(settings, rw, s))
                            return;
                    }
                }
            }
        }
        if (rw.startsWith(guild.getSelfMember().getAsMention())) {
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
            return;
        }
        //Handle the command
        commandManager.runCommand(event);
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

        if (settings.isAutoroleEnabled()
                && guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            Role r = guild.getRoleById(settings.getAutoroleRole());
            if (r != null && !guild.getPublicRole().equals(r) && guild.getSelfMember().canInteract(r))
                guild.getController()
                        .addSingleRoleToMember(event.getMember(), r).queue(null, it -> {
                });
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        Guild guild = event.getGuild();
        if (event.getMember().equals(guild.getSelfMember())) return;
        GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);

        if (settings.isEnableJoinMessage()) {
            long welcomeLeaveChannelId =
                    (settings.getWelcomeLeaveChannel() <= 0)
                            ? GuildUtils.getPublicChannel(guild).getIdLong() : settings.getWelcomeLeaveChannel();
            TextChannel welcomeLeaveChannel = guild.getTextChannelById(welcomeLeaveChannelId);
            String msg = parseGuildVars(settings.getCustomLeaveMessage(), event);
            if (!msg.isEmpty() || "".equals(msg) || welcomeLeaveChannel != null)
                sendMsg(welcomeLeaveChannel, msg);
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        //if 70 of a guild is bots, we'll leave it
        double[] botToUserRatio = GuildUtils.getBotRatio(guild);
        long[] counts = GuildUtils.getBotAndUserCount(guild);
        long members = guild.getMemberCache().size();
        if (botToUserRatio[1] >= 70 && !botLists.contains(guild.getIdLong()) && members > 30) {
            sendMsg(GuildUtils.getPublicChannel(guild),
                    String.format("Hey %s, %s%s of this guild are bots (%s is the total btw). I'm outta here.",
                            guild.getOwner().getAsMention(),
                            botToUserRatio[1],
                            "%",
                            guild.getMemberCache().size()
                    ),
                    message -> message.getGuild().leave().queue(),
                    er -> guild.leave().queue()
            );
            /*logger.info(TextColor.RED + String.format("Joining guild: %s, and leaving it after. BOT ALERT (%s/%s)",
                    guild.getName(),
                    counts[0],
                    counts[1]) + TextColor.RESET);*/
            logger.info("{}Joining guild: {}, and leaving it after. BOT ALTER ({}/{}){}",
                    TextColor.RED,
                    guild.getName(),
                    counts[0],
                    counts[1],
                    TextColor.RESET
            );
            botFamrs.add(guild.getIdLong());
            return;
        }
        /*String message = String.format("Joining guild %s, ID: %s on shard %s.", guild.getName(), guild.getId(), guild.getJDA().getShardInfo()
                .getShardId());
        logger.info(TextColor.GREEN + message + TextColor.RESET);*/

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
        if (!botFamrs.contains(guild.getIdLong())) {
            logger.info(TextColor.RED + "Leaving guild: " + guild.getName() + "." + TextColor.RESET);
            //GuildSettingsUtils.deleteGuild(guild, database);
        } else {
            botFamrs.remove(guild.getIdLong());
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Guild guild = event.getGuild();
        if (LavalinkManager.ins.isConnected(guild)
                && !event.getVoiceState().getMember().equals(guild.getSelfMember())) {
            VoiceChannel vc = LavalinkManager.ins.getConnectedChannel(guild);
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
        try {
            if (LavalinkManager.ins.isConnected(guild)) {
                if (event.getChannelJoined().equals(LavalinkManager.ins.getConnectedChannel(guild))
                        && !event.getMember().equals(guild.getSelfMember())) {
                    return;
                } else {
                    channelCheckThing(guild, LavalinkManager.ins.getConnectedChannel(guild));
                }
                if (event.getChannelLeft().equals(LavalinkManager.ins.getConnectedChannel(guild))) {
                    channelCheckThing(guild, event.getChannelLeft());
                    //return;
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        if(!shuttingDown) return;

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
        if (!isUpdating)
            System.exit(0);
    }

    /*
     * Needs a better name
     */
    private boolean startsWithPrefix(GuildSettings settings, String rw, String s) {
        return s.equalsIgnoreCase(rw.replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(settings.getCustomPrefix()), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase());
    }
    //                                    raw,    category?
    private boolean shouldBlockCommand(String rw, String s) {
        return commandManager.getCommand(rw.replaceFirst(Pattern.quote(Settings.OTHER_PREFIX), Settings.PREFIX)
                .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase())
                .getCategory() == CommandCategory.valueOf(s.toUpperCase());
    }

    /**
     * This handles the guild leave/ join events to deferments if the channel is empty
     *
     * @param g  the guild
     * @param vc the voice channel
     */
    private void channelCheckThing(Guild g, VoiceChannel vc) {

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

    private String parseGuildVars(String message, GenericGuildMemberEvent event) {

        if (!(event instanceof GuildMemberJoinEvent) && !(event instanceof GuildMemberLeaveEvent))
            return "NOPE";

        Guild guild = event.getGuild();
        GuildSettings s = GuildSettingsUtils.getGuild(guild, variables);
        long welcomeLeaveChannel = s.getWelcomeLeaveChannel();
        long autoRoleId = s.getAutoroleRole();

        message = CustomCommandUtils.PARSER.clear()
                .put("user", event.getUser())
                .put("guild", event.getGuild())
                .put("channel", event.getGuild().getTextChannelById(welcomeLeaveChannel))
                .put("args", "")
                .parse(message);

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
    private boolean isCategory(String name) {
        try {
            return CommandCategory.valueOf(name.toUpperCase()) != null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private void killAllShards(ShardManager manager) {
        manager.shutdown();
        /*manager.getShards().forEach(jda -> {
            logger.info(String.format("Shard %s has been shut down", jda.getShardInfo().getShardId()));
            jda.shutdown();
        });*/
    }

}
