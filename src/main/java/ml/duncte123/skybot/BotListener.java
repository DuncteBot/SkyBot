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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class BotListener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(BotListener.class);

    /**
     * This filter helps us to fiter out swearing
     */
    private final BadWordFilter filter = new BadWordFilter();

    /**
     * When a command gets ran, it'll be stored in here
     */
    private static final Map<Guild, TextChannel> lastGuildChannel = new HashMap<>();

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
        if (event.getMessage().getContentRaw().equals(Settings.prefix + "shutdown")
                    && Arrays.asList(Settings.wbkxwkZPaG4ni5lm8laY).contains(event.getAuthor().getId())) {
            logger.info("Initialising shutdown!!!");

            MusicCommand.shutdown();

            event.getMessage().addReaction("✅").queue(
                    success->killAllShards(event.getJDA().asBot().getShardManager()),
                    failure->killAllShards(event.getJDA().asBot().getShardManager())
            );

            //Kill other things
            ((EvalCommand) AirUtils.commandManager.getCommand("eval")).shutdown();
            if (unbanTimerRunning)
                this.unbanService.shutdown();
            
            if (settingsUpdateTimerRunning)
                this.settingsUpdateService.shutdown();
            
            AirUtils.stop();
            
            System.exit(0);
        }

        if (event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)
                    && settings.isEnableSwearFilter()
                    && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                Message messageToCheck = event.getMessage();
                if (filter.filterText(messageToCheck.getContentRaw())) {
                    messageToCheck.delete().reason("Blocked for bad swearing: " + messageToCheck.getContentDisplay())
                            .queue(null, CUSTOM_QUEUE_ERROR);
                    event.getChannel().sendMessage(
                            String.format("Hello there, %s please do not use cursive language within this Discord.",
                                    event.getAuthor().getAsMention())).queue(
                            m -> m.delete().queueAfter(10, TimeUnit.SECONDS, null, CUSTOM_QUEUE_ERROR));
                    return;
                }
        }
        
        String rw = event.getMessage().getContentRaw();
        
        if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser()) && event.getChannel().getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)
                && rw.equals(event.getGuild().getSelfMember().getAsMention())) {
            event.getChannel().sendMessage(
                    String.format("Hey <@%s>, try `%shelp` for a list of commands. If it doesn't work scream at _duncte123#1245_",
                            event.getAuthor().getId(),
                            Settings.prefix)
            ).queue();
            return;
        }else if (!rw.toLowerCase().startsWith(Settings.prefix.toLowerCase()) &&
                !rw.startsWith(settings.getCustomPrefix())
                && !rw.startsWith(event.getGuild().getSelfMember().getAsMention())
                && !rw.toLowerCase().startsWith(Settings.otherPrefix.toLowerCase())) {
            return;
        }

        //If the topic contains -commands ignore it
        if (event.getChannel().getTopic() != null) {
            String[] blocked = event.getChannel().getTopic().split("-");
            if (event.getChannel().getTopic().contains("-commands"))
                return;
            for (String s : blocked) {
                if (isCategory(s.toUpperCase())) {
                    if (AirUtils.commandManager.getCommands(CommandCategory.valueOf(s.toUpperCase()))
                            .contains(AirUtils.commandManager.getCommand(rw.replaceFirst(Settings.otherPrefix, Settings.prefix)
                                    .replaceFirst(Pattern.quote(Settings.prefix), "").split("\\s+",2)[0].toLowerCase()))) {
                        return;
                    }
                } else {
                    if (s.toLowerCase().equals(rw.replaceFirst(Settings.otherPrefix, Settings.prefix)
                            .replaceFirst(Pattern.quote(Settings.prefix), "").split("\\s+",2)[0].toLowerCase()))
                        return;
                }
            }
        }
        if (rw.startsWith(event.getGuild().getSelfMember().getAsMention()) ) {
            final String[] split = rw.replaceFirst(Pattern.quote(Settings.prefix), "").split("\\s+");
            final String[] args = Arrays.copyOfRange(split, 1, split.length);
            //Handle the chat command
            Command cmd = AirUtils.commandManager.getCommand("chat");
            if (cmd != null)
                cmd.executeCommand("chat", args, event);
            return;
        }
        //Store the channel
        lastGuildChannel.put(event.getGuild(), event.getChannel());
        //Handle the command
        AirUtils.commandManager.runCommand(event);
    }
    
    /**
     * When the bot is ready to go
     *
     * @param event The corresponding {@link ReadyEvent}
     */
    @Override
    public void onReady(ReadyEvent event){
        logger.info("Logged in as " + String.format("%#s (Shard #%s)", event.getJDA().getSelfUser(), event.getJDA().getShardInfo().getShardId()));

        if (Settings.isUnstable) {
            if (event.getJDA().getSelfUser().getIdLong() == 210363111729790977L)
                return;
            //noinspection unchecked
            List<Long> ids = (List<Long>) AirUtils.config.getArray("access_ids");
            event.getJDA().getGuilds().forEach(g -> {
                if (!ids.contains(g.getIdLong())) {
                    g.leave().queue();
                    logger.info(TextColor.ORANGE+"Leaving Guild: "+g.getName()+", because its not authorized for/in the UNSTABLE project."+TextColor.RESET);
                }
            });
        }

        //Start the timers if they have not been started yet
        if (!unbanTimerRunning && AirUtils.nonsqlite) {
            logger.info("Starting the unban timer.");
            //Register the timer for the auto unbans
            unbanService.scheduleAtFixedRate(() -> ModerationUtils.checkUnbans(event.getJDA().asBot().getShardManager()),10, 10, TimeUnit.MINUTES);
            unbanTimerRunning = true;
        }
        
        if (!settingsUpdateTimerRunning && AirUtils.nonsqlite) {
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
            if (msg.isEmpty() || welcomeLeaveChannel == null)
                return;
            welcomeLeaveChannel.sendMessage(msg).queue();
        }

        if(settings.getAutoroleRole() != null && !"".equals(settings.getAutoroleRole()) && event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            Role r = event.getGuild().getRoleById(settings.getAutoroleRole());
            if(r != null && !event.getGuild().getPublicRole().equals(r))
                event.getGuild().getController().addSingleRoleToMember(event.getMember(), r).queue(null, it -> {
                    TextChannel tc = GuildUtils.getPublicChannel(event.getGuild());
                    if(tc != null && event.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ))
                        tc.sendMessage("Error while trying to add a role to a user: " + it.toString() + "\n" +
                                "Make sure that the role " + r.getAsMention() + " is below my role").queue();
                });
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
            if (msg.isEmpty() || welcomeLeaveChannel == null)
               return;
            welcomeLeaveChannel.sendMessage(msg).queue();
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
        if (botToUserRatio[1] >= 60) {
            GuildUtils.getPublicChannel(event.getGuild()).sendMessage(String.format("Hey %s, %s%s of this guild are bots (%s is the total btw). Iḿ outta here.",
                    event.getGuild().getOwner().getAsMention(),
                    botToUserRatio[1],
                    "%",
                    event.getGuild().getMemberCache().size())).queue(
                    message -> message.getGuild().leave().queue()
            );
            logger.info(TextColor.RED + "Joining guild: " + event.getGuild().getName() + ", and leaving it after. BOT ALERT" + TextColor.RESET);
            return;
        }
        Guild g = event.getGuild();
        if (Settings.isUnstable) {
            if (event.getJDA().getSelfUser().getIdLong() == 210363111729790977L)
                return;
            //noinspection unchecked
            List<Long> ids = (List<Long>) AirUtils.config.getArray("access_ids");
            if (!ids.contains(g.getIdLong())) {
                g.leave().queue();
                logger.info(TextColor.ORANGE+"Leaving Guild: "+g.getName()+", because its not authorized for/in the UNSTABLE project."+TextColor.RESET);
                return;
            }
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
        if(event.getGuild().getAudioManager().isConnected()) {
            if (!event.getVoiceState().getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId())) {
                if (!event.getChannelLeft().getId().equals(event.getGuild().getAudioManager().getConnectedChannel().getId())) {
                    return;
                }
                channelCheckThing(event.getGuild(), event.getChannelLeft());
                MusicCommand.cooldowns.put(event.getGuild().getIdLong(), 12600);
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
        if(event.getGuild().getAudioManager().isConnected()) {
            if (!event.getVoiceState().getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId())) {
                if (!event.getChannelLeft().getId().equals(event.getGuild().getAudioManager().getConnectedChannel().getId())) {
                    return;
                }
                channelCheckThing(event.getGuild(), event.getChannelLeft());

                if (event.getGuild().getAudioManager().getConnectedChannel() != null &&
                        !event.getChannelJoined().getId().equals(event.getGuild().getAudioManager().getConnectedChannel().getId())) {
                    return;
                    //System.out.println("Self (this might be buggy)");
                }
                channelCheckThing(event.getGuild(), event.getChannelJoined());
            }
        }
    }

    /**
     * This handles the guild leave/ join events to deferments if the channel is empty
     * @param g the guild
     * @param vc the voice channel
     */
    private void channelCheckThing(Guild g, VoiceChannel vc) {

        if (vc.getMembers().stream().filter(m -> !m.getUser().isBot()).count() < 1) {
            GuildMusicManager manager = AirUtils.audioUtils.getMusicManager(g);
            manager.player.stopTrack();
            manager.player.setPaused(false);
            manager.scheduler.queue.clear();

            TextChannel textChannel = lastGuildChannel.get(g);
            if (g.getSelfMember().hasPermission(textChannel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ))
                textChannel.sendMessage("Leaving voice channel because all the members have left it.").queue();
            if (g.getAudioManager().isConnected()) {
                g.getAudioManager().closeAudioConnection();
                g.getAudioManager().setSendingHandler(null);
                if(AirUtils.audioUtils.getMusicManagers().containsKey(g.getId()))
                    AirUtils.audioUtils.getMusicManagers().remove(g.getId());
            }
        }
    }

    private String parseGuildVars(String message, GenericGuildMemberEvent event) {

        if(!(event instanceof GuildMemberJoinEvent) && !(event instanceof GuildMemberLeaveEvent))
            return "NOPE";

        return message.replaceAll("\\{\\{USER_MENTION}}", event.getUser().getAsMention())
                .replaceAll("\\{\\{USER_NAME}}", event.getUser().getName())
                .replaceAll("\\{\\{USER_FULL}}", String.format("%#s", event.getUser()))
                .replaceAll("\\{\\{IS_USER_BOT}}", String.valueOf(event.getUser().isBot()))
                .replaceAll("\\{\\{GUILD_NAME}}", event.getGuild().getName())
                .replaceAll("\\{\\{GUILD_USER_COUNT}}", event.getGuild().getMemberCache().size() + "")

                //This one can be kept a secret :P
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
}
