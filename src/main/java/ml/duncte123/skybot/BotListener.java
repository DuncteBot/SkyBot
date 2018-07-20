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
import ml.duncte123.skybot.commands.uncategorized.UserinfoCommand;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.ICommand;
import ml.duncte123.skybot.objects.command.MusicCommand;
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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotListener extends ListenerAdapter {

    private static final Pattern DISCORD_INVITE_PATTERN = Pattern.compile("discord(?:app\\.com/invite|\\.gg)/([\\S\\w]*\\b)");
    private final Logger logger = LoggerFactory.getLogger(BotListener.class);

    /**
     * Check if we are updating
     */
    public static boolean isUpdating = false;

    /**
     * This filter helps us to fiter out swearing
     */
    private final BadWordFilter wordFilter = new BadWordFilter();
    /**
     * This filter helps us to fiter out spam
     */
    private final SpamFilter spamFilter = new SpamFilter();
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
    private final HashMap<String, String> badGuilds = new HashMap<>();
    /**
     * This tells us if the {@link #systemPool} is running
     */
    private boolean unbanTimerRunning = false;
    /**
     * Tells us whether {@link #systemPool} clears cache of our {@link #spamFilter}.
     */
    private boolean isCacheCleanerActive = false;

    @Override
    public void onShutdown(ShutdownEvent event) {
        MusicCommand.shutdown();

        //Kill other things
        //((EvalCommand) AirUtils.COMMAND_MANAGER.getCommand("eval")).shutdown();
        if (unbanTimerRunning && isCacheCleanerActive)
            this.systemPool.shutdown();

        //clear the userinfo folder on shutdown as well
        String imgDir = ((UserinfoCommand) AirUtils.COMMAND_MANAGER.getCommand("userinfo")).getFolderName();
        try {
            FileUtils.cleanDirectory(new File(imgDir));
        } catch (IOException e) {
            e.printStackTrace();
        }

        AirUtils.stop();

        /*
         * Only shut down if we are not updating
         */
        if(!isUpdating)
            System.exit(0);
    }

    /**
     * Listen for messages send to the bot
     *
     * @param event The corresponding {@link GuildMessageReceivedEvent}
     */
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        //We only want to respond to members/users
        if (event.getAuthor().isFake() || event.getAuthor().isBot() || event.getMember() == null)
            return;
        //noinspection deprecation
        if (event.getMessage().getContentRaw().equals(Settings.PREFIX + "shutdown")
                && Settings.wbkxwkZPaG4ni5lm8laY.contains(event.getAuthor().getIdLong())) {
            logger.info("Initialising shutdown!!!");

            event.getMessage().addReaction("✅").queue(
                    success -> killAllShards(event.getJDA().asBot().getShardManager()),
                    failure -> killAllShards(event.getJDA().asBot().getShardManager())
            );

            return;
        }

        Guild guild = event.getGuild();
        GuildSettings settings = GuildSettingsUtils.getGuild(guild);
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
                                    MessageUtils.sendMsg(event, event.getAuthor().getAsMention() +
                                            ", please don't post invite links here.", m -> m.delete().queueAfter(4, TimeUnit.SECONDS))
                            );
                        }
                    }, (__) -> {}/*, (thr) -> {
                    try {
                        throw new SkybotContextException(thr.getMessage(), thr);
                    } catch (SkybotContextException e) {
                        MessageUtils.sendMsg(event, "I can not read the guild invites due to a lack of permissions.\n" +
                                "Grant the permission `MANAGE_SERVER` for me.\n" +
                                "Error: " +  e.getMessage());
                    }
                }*/);
                }
            }/* else {
                MessageUtils.sendMsg(event, "I can not read the guild invites due to a lack of permissions.\n" +
                        "Grant the permission `MANAGE_SERVER` for me.");
            }*/

            if (settings.isEnableSwearFilter()) {
                Message messageToCheck = event.getMessage();
                if (wordFilter.filterText(rw)) {
                    messageToCheck.delete().reason("Blocked for bad swearing: " + messageToCheck.getContentDisplay())
                            .queue(null, CUSTOM_QUEUE_ERROR);

                    MessageUtils.sendMsg(event,
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
                if (spamFilter.check(new Triple<>(event.getMember(), messageToCheck, settings.getKickState()))) {
                    ModerationUtils.modLog(event.getJDA().getSelfUser(), event.getAuthor(),
                            settings.getKickState() ? "kicked" : "muted", "spam", guild);
                }
            }
        }

        if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())
                && rw.equals(guild.getSelfMember().getAsMention())) {
            MessageUtils.sendMsg(event, String.format("Hey <@%s>, try `%shelp` for a list of commands. If it doesn't work scream at _duncte123#1245_",
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
                        if (isaBoolean(settings, rw, s))
                            return;
                    }
                } else {
                    if (isCategory(s.toUpperCase())) {
                        if (shouldBlockCommand(rw, s)) {
                            return;
                        }
                    } else {
                        if (isaBoolean(settings, rw, s))
                            return;
                    }
                }
            }
        }
        if (rw.startsWith(guild.getSelfMember().getAsMention())) {
            final String[] split = rw.replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+");
            //Handle the chat command
            ICommand cmd = AirUtils.COMMAND_MANAGER.getCommand("chat");
            if (cmd != null)
                cmd.executeCommand("chat", Arrays.copyOfRange(split, 1, split.length), event);
            return;
        }
        //Handle the command
        AirUtils.COMMAND_MANAGER.runCommand(event);
    }

    /*
     * Needs a better name
     */
    private boolean isaBoolean(GuildSettings settings, String rw, String s) {
        return s.equalsIgnoreCase(rw.replaceFirst(Settings.OTHER_PREFIX, Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(settings.getCustomPrefix()), Pattern.quote(Settings.PREFIX))
                .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase());
    }

    private boolean shouldBlockCommand(String rw, String s) {
        return AirUtils.COMMAND_MANAGER.getCommands(CommandCategory.valueOf(s.toUpperCase()))
                .contains(AirUtils.COMMAND_MANAGER.getCommand(rw.replaceFirst(Settings.OTHER_PREFIX, Settings.PREFIX)
                        .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+", 2)[0].toLowerCase()));
    }

    /**
     * When the bot is ready to go
     *
     * @param event The corresponding {@link ReadyEvent}
     */
    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Logged in as " + String.format("%#s (Shard #%s)", event.getJDA().getSelfUser(), event.getJDA().getShardInfo().getShardId()));

        //Start the timers if they have not been started yet
        if (!unbanTimerRunning && AirUtils.NONE_SQLITE) {
            logger.info("Starting the unban timer.");
            //Register the timer for the auto unbans
            systemPool.scheduleAtFixedRate(() ->
                    ModerationUtils.checkUnbans(event.getJDA().asBot().getShardManager()), 5, 5, TimeUnit.MINUTES);
            unbanTimerRunning = true;
        }

        if (!isCacheCleanerActive) {
            logger.info("Starting spam-cache-cleaner!");
            systemPool.scheduleAtFixedRate(spamFilter::clearMessages, 20, 13, TimeUnit.SECONDS);
            isCacheCleanerActive = true;
        }

        //Update guild count from then the bot was offline (should never die tho)
        GuildUtils.updateGuildCountAndCheck(event.getJDA());
    }

    /**
     * This will fire when a new member joins
     *
     * @param event The corresponding {@link GuildMemberJoinEvent}
     */
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

        GuildSettings settings = GuildSettingsUtils.getGuild(guild);

        if (settings.isEnableJoinMessage()) {
            String welcomeLeaveChannelId = (settings.getWelcomeLeaveChannel() == null || "".equals(settings.getWelcomeLeaveChannel())
                    ? GuildUtils.getPublicChannel(guild).getId() : settings.getWelcomeLeaveChannel());
            TextChannel welcomeLeaveChannel = guild.getTextChannelById(welcomeLeaveChannelId);
            String msg = parseGuildVars(settings.getCustomJoinMessage(), event);
            if (!msg.isEmpty() || "".equals(msg) || welcomeLeaveChannel != null)
                MessageUtils.sendMsg(welcomeLeaveChannel, msg);
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
        GuildSettings settings = GuildSettingsUtils.getGuild(guild);

        if (settings.isEnableJoinMessage()) {
            String welcomeLeaveChannelId =
                    (settings.getWelcomeLeaveChannel() == null || settings.getWelcomeLeaveChannel().isEmpty())
                            ? GuildUtils.getPublicChannel(guild).getId() : settings.getWelcomeLeaveChannel();
            TextChannel welcomeLeaveChannel = guild.getTextChannelById(welcomeLeaveChannelId);
            String msg = parseGuildVars(settings.getCustomLeaveMessage(), event);
            if (!msg.isEmpty() || "".equals(msg) || welcomeLeaveChannel != null)
                MessageUtils.sendMsg(welcomeLeaveChannel, msg);
        }
    }

    /**
     * This will fire when the bot joins a guild and we check if we are allowed to join this guild
     *
     * @param event The corresponding {@link GuildJoinEvent}
     */
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        //if 70 of a guild is bots, we'll leave it
        double[] botToUserRatio = GuildUtils.getBotRatio(guild);
        long[] counts = GuildUtils.getBotAndUserCount(guild);
        if (botToUserRatio[1] >= 70) {
            MessageUtils.sendMsg(GuildUtils.getPublicChannel(guild),
                    String.format("Hey %s, %s%s of this guild are bots (%s is the total btw). I'm outta here.",
                            guild.getOwner().getAsMention(),
                            botToUserRatio[1],
                            "%",
                            guild.getMemberCache().size()
                    ),
                    message -> message.getGuild().leave().queue(),
                    er -> guild.leave().queue()
            );
            logger.info(TextColor.RED + String.format("Joining guild: %s, and leaving it after. BOT ALERT (%s/%s)",
                    guild.getName(),
                    counts[0],
                    counts[1]) + TextColor.RESET);
            badGuilds.put(guild.getId(), "BAD BOI");
            return;
        }
        String message = String.format("Joining guild %s, ID: %s on shard %s.", guild.getName(), guild.getId(), guild.getJDA().getShardInfo()
                .getShardId());
        logger.info(TextColor.GREEN + message + TextColor.RESET);
        GuildSettingsUtils.registerNewGuild(guild);
        GuildUtils.updateGuildCountAndCheck(event.getJDA());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        if (!badGuilds.containsKey(guild.getId())) {
            logger.info(TextColor.RED + "Leaving guild: " + guild.getName() + "." + TextColor.RESET);
            GuildSettingsUtils.deleteGuild(guild);
            GuildUtils.updateGuildCountAndCheck(event.getJDA());
        } else {
            badGuilds.remove(guild.getId());
        }
    }

    /**
     * This will fire when a member leaves a channel in a guild, we check if the channel is empty and if it is we leave it
     *
     * @param event {@link GuildVoiceLeaveEvent}
     */
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

    /**
     * This will fire when a member moves from channel, if a member moves we will check if our channel is empty
     *
     * @param event {@link GuildVoiceMoveEvent}
     */
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

    /**
     * This handles the guild leave/ join events to deferments if the channel is empty
     *
     * @param g  the guild
     * @param vc the voice channel
     */
    private void channelCheckThing(Guild g, VoiceChannel vc) {

        if (vc.getMembers().stream().filter(m -> !m.getUser().isBot()).count() < 1) {
            GuildMusicManager manager = AudioUtils.ins.getMusicManager(g);
            manager.player.stopTrack();
            manager.player.setPaused(false);
            manager.scheduler.queue.clear();
            MusicCommand.cooldowns.put(g.getIdLong(), 12600);

            if (g.getAudioManager().getConnectionListener() != null)
                g.getAudioManager().setConnectionListener(null);

            MessageUtils.sendMsg(manager.latestChannel, "Leaving voice channel because all the members have left it.");
            if (LavalinkManager.ins.isConnected(g)) {
                LavalinkManager.ins.closeConnection(g);
                AudioUtils.ins.getMusicManagers().remove(g.getId());
            }
        }
    }

    private String parseGuildVars(String message, GenericGuildMemberEvent event) {

        if (!(event instanceof GuildMemberJoinEvent) && !(event instanceof GuildMemberLeaveEvent))
            return "NOPE";

        Guild guild = event.getGuild();
        String autoRoleId = GuildSettingsUtils.getGuild(guild).getAutoroleRole();

        return message.replaceAll("\\{\\{USER_MENTION}}", event.getUser().getAsMention())
                .replaceAll("\\{\\{USER_NAME}}", event.getUser().getName())
                .replaceAll("\\{\\{USER_FULL}}", String.format("%#s", event.getUser()))
                .replaceAll("\\{\\{IS_USER_BOT}}", String.valueOf(event.getUser().isBot()))
                .replaceAll("\\{\\{GUILD_NAME}}", guild.getName())
                .replaceAll("\\{\\{GUILD_USER_COUNT}}", guild.getMemberCache().size() + "")

                //This one can be kept a secret :P
                .replaceAll("\\{\\{AUTO_ROLE_NAME}", autoRoleId == null || autoRoleId.isEmpty() ?
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
