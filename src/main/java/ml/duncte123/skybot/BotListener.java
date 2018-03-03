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
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.commands.essentials.eval.EvalCommand;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.*;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotListener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(BotListener.class);

    /**
     * This filter helps us to fiter out swearing
     */
    private final BadWordFilter filter = new BadWordFilter();

    /**
     * This timer is for checking unbans
     */
    private final ScheduledExecutorService unbanService = Executors.newScheduledThreadPool(1,
            r -> new Thread(r, "Unban-Thread"));

    /**
     * This tells us if the {@link #unbanService} is running
     */
    private boolean unbanTimerRunning = false;

    /**
     * This timer is for checking new quotes
     */
    private final ScheduledExecutorService settingsUpdateService = Executors.newScheduledThreadPool(1,
            r -> new Thread(r, "Settings-Thread"));

    /**
     * This tells us if the {@link #settingsUpdateService} is running
     */
    private boolean settingsUpdateTimerRunning = false;

    private static final Pattern DISCORD_INVITE_PATTERN = Pattern.compile("(?:https?://)(?:www\\.)?discord(?:app)?\\.(?:com|gg)+/(?:invite/)*([A-Za-z_0-9]+)");


    /**
     * A custom consumer that cancels the stupid unknown message error
     */
    private final Consumer<Throwable> CUSTOM_QUEUE_ERROR = it -> {
        if(it instanceof ErrorResponseException){
            if(((ErrorResponseException) it).getErrorCode() != 10008)
                logger.error("RestAction queue returned failure", it);
        }
    };

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
        
        GuildSettings settings = GuildSettingsUtils.getGuild(event.getGuild());

        //noinspection deprecation
        if (event.getMessage().getContentRaw().equals(Settings.PREFIX + "shutdown")
                    && Arrays.asList(Settings.wbkxwkZPaG4ni5lm8laY).contains(event.getAuthor().getId())) {
            logger.info("Initialising shutdown!!!");

            MusicCommand.shutdown();

            event.getMessage().addReaction("✅").queue(
                    success->killAllShards(event.getJDA().asBot().getShardManager()),
                    failure->killAllShards(event.getJDA().asBot().getShardManager())
            );

            if(LavalinkManager.ins.isEnabled()) {
                LavalinkManager.ins.getLavalink().shutdown();
            }

            //Kill other things
            ((EvalCommand) AirUtils.COMMAND_MANAGER.getCommand("eval")).shutdown();
            if (unbanTimerRunning)
                this.unbanService.shutdown();
            
            if (settingsUpdateTimerRunning)
                this.settingsUpdateService.shutdown();
            
            AirUtils.stop();

            try {
                Thread.sleep(3 * 1000);
                System.exit(0);
            }
            catch (InterruptedException ignored) {}

            return;
        }

        String rw = event.getMessage().getContentRaw();

        if (event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)
                    && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {


            if(settings.isEnableSwearFilter()) {
                Message messageToCheck = event.getMessage();
                if (filter.filterText(rw)) {
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

            if(settings.isFilterInvites()) {
                Matcher matcher = DISCORD_INVITE_PATTERN.matcher(rw);
                if(matcher.find()) {
                    //Get the invite Id from the message
                    String inviteID = matcher.group(matcher.groupCount());

                    Invite.resolve(event.getJDA(), inviteID).queue(invite -> {
                        //Check if the invite is for this guild, if it is not delete the message
                        if(!invite.getGuild().getId().equals(event.getGuild().getId())) {
                            event.getMessage().delete().reason("Contained Invite").queue(it ->
                                    MessageUtils.sendMsg(event, event.getAuthor().getAsMention() +
                                            ", please don't post invite links here", m -> m.delete().queueAfter(3, TimeUnit.SECONDS))
                            );
                        }
                    });
                }
            }
        }
        
        if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())
                && rw.equals(event.getGuild().getSelfMember().getAsMention())) {
            MessageUtils.sendMsg(event, String.format("Hey <@%s>, try `%shelp` for a list of commands. If it doesn't work scream at _duncte123#1245_",
                    event.getAuthor().getId(),
                    Settings.PREFIX)
            );
            return;
        }else if (!rw.toLowerCase().startsWith(Settings.PREFIX.toLowerCase()) &&
                !rw.startsWith(settings.getCustomPrefix())
                && !rw.startsWith(event.getGuild().getSelfMember().getAsMention())
                && !rw.toLowerCase().startsWith(Settings.OTHER_PREFIX.toLowerCase())) {
            return;
        }

        //If the topic contains -commands ignore it
        if (event.getChannel().getTopic() != null) {
            String[] blocked = event.getChannel().getTopic().split("-");
            if (event.getChannel().getTopic().contains("-commands"))
                return;
            for (String s : blocked) {
                if (isCategory(s.toUpperCase())) {
                    if (AirUtils.COMMAND_MANAGER.getCommands(CommandCategory.valueOf(s.toUpperCase()))
                            .contains(AirUtils.COMMAND_MANAGER.getCommand(rw.replaceFirst(Settings.OTHER_PREFIX, Settings.PREFIX)
                                    .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+",2)[0].toLowerCase()))) {
                        return;
                    }
                } else {
                    if (s.toLowerCase().equals(rw.replaceFirst(Settings.OTHER_PREFIX, Settings.PREFIX)
                            .replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+",2)[0].toLowerCase()))
                        return;
                }
            }
        }
        if (rw.startsWith(event.getGuild().getSelfMember().getAsMention()) ) {
            final String[] split = rw.replaceFirst(Pattern.quote(Settings.PREFIX), "").split("\\s+");
            final String[] args = Arrays.copyOfRange(split, 1, split.length);
            //Handle the chat command
            Command cmd = AirUtils.COMMAND_MANAGER.getCommand("chat");
            if (cmd != null)
                cmd.executeCommand("chat", args, event);
            return;
        }
        //Handle the command
        AirUtils.COMMAND_MANAGER.runCommand(event);
    }
    
    /**
     * When the bot is ready to go
     *
     * @param event The corresponding {@link ReadyEvent}
     */
    @Override
    public void onReady(ReadyEvent event){
        logger.info("Logged in as " + String.format("%#s (Shard #%s)", event.getJDA().getSelfUser(), event.getJDA().getShardInfo().getShardId()));

        SelfUser selfUser = event.getJDA().getSelfUser();
        for (Guild guild : event.getJDA().getGuilds()) {
            unstableCheckThingy(selfUser, guild);
        }

        //Start the timers if they have not been started yet
        if (!unbanTimerRunning && AirUtils.NONE_SQLITE) {
            logger.info("Starting the unban timer.");
            //Register the timer for the auto unbans
            unbanService.scheduleAtFixedRate(() -> ModerationUtils.checkUnbans(event.getJDA().asBot().getShardManager()),10, 10, TimeUnit.MINUTES);
            unbanTimerRunning = true;
        }
        
        if (!settingsUpdateTimerRunning && AirUtils.NONE_SQLITE) {
            logger.info("Starting the settings timer.");
            //This handles the updating from the setting and quotes
            settingsUpdateService.scheduleWithFixedDelay(GuildSettingsUtils::loadAllSettings, 1, 1, TimeUnit.HOURS);
            settingsUpdateTimerRunning = true;
        }
        
        //Update guild count from then the bot was offline (should never die tho)
        GuildUtils.updateGuildCountAndCheck(event.getJDA(), event.getJDA().asBot().getShardManager().getGuildCache().size());
    }
    
    /**
     * This will fire when a new member joins
     *
     * @param event The corresponding {@link GuildMemberJoinEvent}
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        /*
        {{USER_MENTION}} = mention user
        {{USER_NAME}} = return username
        {{GUILD_NAME}} = the name of the guild
        {{GUILD_USER_COUNT}} = member count
        {{GUILD_OWNER_MENTION}} = mention the guild owner
        {{GUILD_OWNER_NAME}} = return the name form the owner
         */
        
        GuildSettings settings = GuildSettingsUtils.getGuild(event.getGuild());
        
        if (settings.isEnableJoinMessage()) {
            String welcomeLeaveChannelId = (settings.getWelcomeLeaveChannel() == null || "".equals(settings.getWelcomeLeaveChannel())
                    ? GuildUtils.getPublicChannel(event.getGuild()).getId() : settings.getWelcomeLeaveChannel());
            TextChannel welcomeLeaveChannel = event.getGuild().getTextChannelById(welcomeLeaveChannelId);
            String msg = parseGuildVars(settings.getCustomJoinMessage(), event);
            if (!msg.isEmpty() || "".equals(msg) || welcomeLeaveChannel != null)
                MessageUtils.sendMsg(welcomeLeaveChannel, msg);
        }

        if(settings.getAutoroleRole() != null && !"".equals(settings.getAutoroleRole())
                && event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            Role r = event.getGuild().getRoleById(settings.getAutoroleRole());
            if(r != null && !event.getGuild().getPublicRole().equals(r))
                event.getGuild().getController().addSingleRoleToMember(event.getMember(), r).queue(null, it -> {});
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        GuildSettings settings = GuildSettingsUtils.getGuild(event.getGuild());

        if (settings.isEnableJoinMessage()) {
            String welcomeLeaveChannelId =
                    (settings.getWelcomeLeaveChannel() == null || settings.getWelcomeLeaveChannel().isEmpty())
                    ? GuildUtils.getPublicChannel(event.getGuild()).getId() : settings.getWelcomeLeaveChannel();
            TextChannel welcomeLeaveChannel = event.getGuild().getTextChannelById(welcomeLeaveChannelId);
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
        //if 70 of a guild is bots, we'll leave it
        double[] botToUserRatio = GuildUtils.getBotRatio(event.getGuild());
        long[] counts = GuildUtils.getBotAndUserCount(event.getGuild());
        if (botToUserRatio[1] >= 60) {
            MessageUtils.sendMsg(GuildUtils.getPublicChannel(event.getGuild()),
                    String.format("Hey %s, %s%s of this guild are bots (%s is the total btw). I'm outta here.",
                    event.getGuild().getOwner().getAsMention(),
                    botToUserRatio[1],
                    "%",
                    event.getGuild().getMemberCache().size()
            ),
                    message -> message.getGuild().leave().queue(),
                    er -> event.getGuild().leave().queue()
            );
            logger.info(TextColor.RED + String.format("Joining guild: %s, and leaving it after. BOT ALERT (%s/%s)",
                    event.getGuild().getName(),
                    counts[0],
                    counts[1]) + TextColor.RESET);
            return;
        }
        Guild g = event.getGuild();
        if (unstableCheckThingy(event.getJDA().getSelfUser(), g)) {
            return;
        }
        String message = String.format("Joining guild %s, ID: %s on shard %s.", g.getName(), g.getId(), g.getJDA().getShardInfo().getShardId());
        logger.info(TextColor.GREEN + message + TextColor.RESET);
        GuildSettingsUtils.registerNewGuild(event.getGuild());
        GuildUtils.updateGuildCountAndCheck(event.getJDA(), event.getJDA().asBot().getShardManager().getGuildCache().size());
    }
    
    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        logger.info(TextColor.RED + "Leaving guild: " + event.getGuild().getName() + "." + TextColor.RESET);
        GuildSettingsUtils.deleteGuild(event.getGuild());
        GuildUtils.updateGuildCountAndCheck(event.getJDA(), event.getJDA().asBot().getShardManager().getGuildCache().size());
    }
    
    /**
     * This will fire when a member leaves a channel in a guild, we check if the channel is empty and if it is we leave it
     *
     * @param event {@link GuildVoiceLeaveEvent}
     */
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if(LavalinkManager.ins.isConnected(event.getGuild())) {
            if (!event.getVoiceState().getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId())) {
                VoiceChannel vc = LavalinkManager.ins.getConnectedChannel(event.getGuild());
                if(vc != null) {
                    if (!event.getChannelLeft().getId().equals(vc.getId())) {
                        return;
                    }
                    channelCheckThing(event.getGuild(), event.getChannelLeft());
                }
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
        if(LavalinkManager.ins.isConnected(event.getGuild())) {
            //if (!event.getVoiceState().getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId())) {
                if (!event.getChannelLeft().getId().equals( LavalinkManager.ins.getConnectedChannel(event.getGuild()).getId() )) {
                    channelCheckThing(event.getGuild(), event.getChannelLeft());
                    //return;
                }
                //channelCheckThing(event.getGuild(), event.getChannelLeft());

                if (!event.getChannelJoined().getId().equals( LavalinkManager.ins.getConnectedChannel(event.getGuild()).getId() )) {
                    return;
                    //System.out.println("Self (this might be buggy)");
                }
                channelCheckThing(event.getGuild(), event.getChannelJoined());
            //}
        }
    }

    /**
     * This handles the guild leave/ join events to deferments if the channel is empty
     * @param g the guild
     * @param vc the voice channel
     */
    private void channelCheckThing(Guild g, VoiceChannel vc) {

        if (vc.getMembers().stream().filter(m -> !m.getUser().isBot()).count() < 1) {
            GuildMusicManager manager = AudioUtils.ins.getMusicManager(g);
            manager.player.stopTrack();
            manager.player.setPaused(false);
            manager.scheduler.queue.clear();
            MusicCommand.cooldowns.put(g.getIdLong(), 12600);

            if(g.getAudioManager().getConnectionListener() != null)
                g.getAudioManager().setConnectionListener(null);

            MessageUtils.sendMsg(manager.latestChannel, "Leaving voice channel because all the members have left it.");
            if (LavalinkManager.ins.isConnected(g)) {
                LavalinkManager.ins.closeConnection(g);
                AudioUtils.ins.getMusicManagers().remove(g.getId());
            }
        }
    }

    private String parseGuildVars(String message, GenericGuildMemberEvent event) {

        if(!(event instanceof GuildMemberJoinEvent) && !(event instanceof GuildMemberLeaveEvent))
            return "NOPE";

        String autoRoleId = GuildSettingsUtils.getGuild(event.getGuild()).getAutoroleRole();

        return message.replaceAll("\\{\\{USER_MENTION}}", event.getUser().getAsMention())
                .replaceAll("\\{\\{USER_NAME}}", event.getUser().getName())
                .replaceAll("\\{\\{USER_FULL}}", String.format("%#s", event.getUser()))
                .replaceAll("\\{\\{IS_USER_BOT}}", String.valueOf(event.getUser().isBot()))
                .replaceAll("\\{\\{GUILD_NAME}}", event.getGuild().getName())
                .replaceAll("\\{\\{GUILD_USER_COUNT}}", event.getGuild().getMemberCache().size() + "")

                //This one can be kept a secret :P
                .replaceAll("\\{\\{AUTO_ROLE_NAME}", autoRoleId == null || autoRoleId.isEmpty() ?
                        "Not set" : event.getGuild().getRoleById(autoRoleId).getName())
                .replaceAll("\\{\\{EVENT_TYPE}}", event instanceof GuildMemberJoinEvent ? "joined" : "left" );
    }

    private boolean isCategory(String name) {
        return name.matches("(?i)ANIMALS|MAIN|FUN|MUSIC|MOD_ADMIN|NERD_STUFF|UNLISTED");
    }

    private void killAllShards(ShardManager manager) {
        manager.getShards().forEach(jda -> {
            logger.info(String.format("Shard %s has been shut down", jda.getShardInfo().getShardId()));
            jda.shutdown();
        });
    }

    private boolean unstableCheckThingy(SelfUser selfUser, Guild g) {
        if (Settings.isUnstable && selfUser.getIdLong() != 210363111729790977L) {
            //noinspection unchecked
            List<Long> ids = AirUtils.CONFIG.getArray("access_ids");
            if (!ids.contains(g.getIdLong())) {
                g.leave().queue();
                logger.info(TextColor.ORANGE + "Leaving Guild: " + g.getName() + ", because its not authorized for/in the UNSTABLE project." + TextColor.RESET);
                return true;
            }
        }
        return false;
    }

}
