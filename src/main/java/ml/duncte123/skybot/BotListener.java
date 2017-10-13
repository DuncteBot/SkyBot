package ml.duncte123.skybot;

import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.parsers.CommandParser;
import ml.duncte123.skybot.utils.*;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class BotListener extends ListenerAdapter {

    /**
     * This is our 'custom' swearword filter
     */
    private final BadWordFilter filter = new BadWordFilter();
    /**
     * This is the command parser
     */
    private static CommandParser parser = new CommandParser();
    /**
     * When a command gets ran, it'll be stored in here
     */
    private static HashMap<Guild, TextChannel> lastGuildChannel = new HashMap<>();
    /**
     * This timer is for checking unbans
     */
    public Timer unbanTimer = new Timer();

    /**
     * Listen for messages send to the bot
     * @param event The corresponding {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){

        //We only want to respond to members/users
        if(event.getAuthor().isFake() || event.getAuthor().isBot() || event.getMember()==null){
            return;
        }

        if(!AirUtils.guildSettings.containsKey(event.getGuild().getId())) {
            GuildSettingsUtils.registerNewGuild(event.getGuild());
        }

        if(event.getMessage().getContent().equals(Settings.prefix + "shutdown") && event.getAuthor().getId().equals(Settings.ownerId)){
            AirUtils.log(Level.INFO,"Shutting down!!!");
            unbanTimer.cancel();
            //event.getJDA().shutdown();
            ShardManager manager = event.getJDA().asBot().getShardManager();
            for(int i = 0; i < manager.getAmountOfTotalShards(); i++) {
                manager.getShardCache().getElementById(i).shutdown();
                AirUtils.log(Level.INFO,"Shard " + i + " has been shut down");
            }
            System.exit(0);
            return;
        }

        Permission[] adminPerms = {
                Permission.MESSAGE_MANAGE
        };
        if(event.getGuild().getSelfMember().hasPermission(adminPerms) && AirUtils.guildSettings.get(event.getGuild().getId()).isEnableSwearFilter()) {
            if (!event.getMember().hasPermission(adminPerms)) {
                Message messageToCheck = event.getMessage();
                if (filter.filterText(messageToCheck.getRawContent())) {
                    messageToCheck.delete().reason("Blocked for bad swearing: " + messageToCheck.getContent()).queue();
                    event.getChannel().sendMessage("Hello there, " + event.getAuthor().getAsMention() + " please do not use cursive language within this Discord.").queue(
                            m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                    return;
                }
            }
        }

        GuildSettings settings = AirUtils.guildSettings.get(event.getGuild().getId());

        if(event.getMessage().getRawContent().startsWith(Settings.prefix) || event.getMessage().getRawContent().startsWith(settings.getCustomPrefix()) ){
            // run the a command
            lastGuildChannel.put(event.getGuild(), event.getChannel());
            AirUtils.commandSetup.runCommand(parser.parse(event.getMessage().getRawContent()
                    .replaceFirst(settings.getCustomPrefix(), Settings.prefix), event));
            //return;
        } else if(event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser()) && event.getChannel().canTalk()) {
            if(event.getMessage().getRawContent().startsWith(event.getJDA().getSelfUser().getAsMention()) && event.getMessage().getRawContent().split(" ").length > 0){
                lastGuildChannel.put(event.getGuild(), event.getChannel());
                AirUtils.commandSetup.runCommand(parser.parse(event.getMessage().getRawContent()
                        .replaceFirst("<@" + event.getJDA().getSelfUser().getId() + "> ", Settings.prefix), event));
                return;
            }
            event.getChannel().sendMessage("Hey <@" + event.getAuthor().getId() + ">, try `" + Settings.prefix + "help` for a list of commands. If it doesn't work scream at _duncte123#1245_").queue();
        }

    }

    /**
     * When the bot is ready to go
     * @param event The corresponding {@link net.dv8tion.jda.core.events.ReadyEvent ReadyEvent}
     */
    @Override
    public void onReady(ReadyEvent event){
        AirUtils.log(Level.INFO, "Logged in as " + String.format("%#s", event.getJDA().getSelfUser()) + " " + event.getJDA().getShardInfo());
    }

    /**
     * This will fire when a new member joins
     * @param event The corresponding {@link net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent GuildMemberJoinEvent}
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){

        /*
        {{USER_MENTION}} = mention user
        {{USER_NAME}} = return username
        {{GUILD_NAME}} = the name of the guild
        {{GUILD_USER_COUNT}} = member count
        {{GUILD_OWNER_MENTION}} = mention the guild owner
        {{GUILD_OWNER_NAME}} = return the name form the owner
         */

        GuildSettings settings = AirUtils.guildSettings.get(event.getGuild().getId());

        if (settings.isEnableJoinMessage()) {
            TextChannel publicChannel = AirUtils.getPublicChannel(event.getGuild());
            String msg = settings.getCustomJoinMessage()
                    .replaceAll("\\{\\{USER_MENTION}}", event.getUser().getAsMention())
                    .replaceAll("\\{\\{USER_NAME}}", event.getUser().getName())
                    .replaceAll("\\{\\{GUILD_NAME}}", event.getGuild().getName())
                    .replaceAll("\\{\\{GUILD_USER_COUNT}}", event.getGuild().getMemberCache().size() + "");
            publicChannel.sendMessage(msg).queue();
        }
    }

    /**
     * This will fire when the bot joins a guild and we check if we are allowed to join this guild
     * @param event The corresponding {@link net.dv8tion.jda.core.events.guild.GuildJoinEvent GuildJoinEvent}
     */
    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        //if 60 of a guild is bots, we'll leave it
        double[] botToUserRatio = AirUtils.getBotRatio(event.getGuild());
        if(botToUserRatio[1] > 60) {
            AirUtils.getPublicChannel(event.getGuild()).sendMessage("Hey " +
                    event.getGuild().getOwner().getAsMention() + ", "+botToUserRatio[1]+"% of this guild are bots ("+event.getGuild().getMemberCache().size()+" is the total btw). " +
                    "I'm outta here").queue(
                            message -> message.getGuild().leave().queue()
            );
            AirUtils.log(Settings.defaultName + "GuildJoin", Level.INFO, "Joining guild: " + event.getGuild().getName() + ", and leaving it after. BOT ALERT");
            return;
        }
        AirUtils.log(Settings.defaultName + "GuildJoin", Level.INFO, "Joining guild: " + event.getGuild().getName() + ".");
        GuildSettingsUtils.registerNewGuild(event.getGuild());
    }

    /**
     * This will fire when a member leaves a channel in a guild, we check if the channel is empty and if it is we leave it
     * @param event {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent GuildVoiceLeaveEvent}
     */
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event){
        if(!event.getVoiceState().getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId()) && event.getGuild().getAudioManager().isConnected()){
            if (!event.getChannelLeft().getId().equals(event.getGuild().getAudioManager().getConnectedChannel().getId())) { return; }
            if(event.getChannelLeft().getMembers().size() <= 1){
                AirUtils.audioUtils.getMusicManager(event.getGuild()).player.stopTrack();
                AirUtils.audioUtils.getMusicManager(event.getGuild()).player.setPaused(false);
                AirUtils.audioUtils.getMusicManager(event.getGuild()).scheduler.queue.clear();
                lastGuildChannel.get(event.getGuild()).sendMessage(EmbedUtils.embedMessage("Leaving voice channel because all the members have left it.")).queue();
                if(event.getGuild().getAudioManager().isConnected()){
                    event.getGuild().getAudioManager().closeAudioConnection();
                    event.getGuild().getAudioManager().setSendingHandler(null);
                }
            }
        }
    }

    /**
     * This will fire when a member moves from channel, if a member moves we will check if our channel is empty
     * @param event {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent GuildVoiceMoveEvent}
     */
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if(!event.getVoiceState().getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId()) && event.getGuild().getAudioManager().isConnected()) {

            if(event.getChannelLeft()!=null) {
                if (!event.getChannelLeft().getId().equals(event.getGuild().getAudioManager().getConnectedChannel().getId())) { return; }
                if(event.getChannelLeft().getMembers().size() <= 1){
                    AirUtils.audioUtils.getMusicManager(event.getGuild()).player.stopTrack();
                    AirUtils.audioUtils.getMusicManager(event.getGuild()).player.setPaused(false);
                    AirUtils.audioUtils.getMusicManager(event.getGuild()).scheduler.queue.clear();
                    lastGuildChannel.get(event.getGuild()).sendMessage(EmbedUtils.embedMessage("Leaving voice channel because all the members have left it.")).queue();
                    if(event.getGuild().getAudioManager().isConnected()){
                        event.getGuild().getAudioManager().closeAudioConnection();
                        event.getGuild().getAudioManager().setSendingHandler(null);
                    }
                }
            }

            /*if(event.getChannelJoined()!=null) {
                if (!event.getChannelJoined().getId().equals(event.getGuild().getAudioManager().getConnectedChannel().getId())) { return; }
                if(event.getChannelJoined().getMembers().size() <= 1){
                    AirUtils.audioUtils.getMusicManager(event.getGuild()).player.stopTrack();
                    AirUtils.audioUtils.getMusicManager(event.getGuild()).player.setPaused(false);
                    AirUtils.audioUtils.getMusicManager(event.getGuild()).scheduler.queue.clear();
                    lastGuildChannel.get(event.getGuild()).sendMessage(AirUtils.embedMessage("Leaving voice channel because all the members have left it.")).queue();
                    if(event.getGuild().getAudioManager().isConnected()){
                        event.getGuild().getAudioManager().setSendingHandler(null);
                        event.getGuild().getAudioManager().closeAudioConnection();
                    }
                }
            }*/
        }
    }

}
