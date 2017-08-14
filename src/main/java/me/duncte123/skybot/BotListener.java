package me.duncte123.skybot;

import me.duncte123.skybot.utils.*;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BotListener extends ListenerAdapter {

    private final BadWordFilter filter = new BadWordFilter();
    private static CommandParser parser = new CommandParser();
    private static HashMap<Guild, TextChannel> lastGuildChannel = new HashMap<>();



    private static Timer timer = new Timer();
    private static Timer unbanTimer = new Timer();

    // listen for messages
    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        if(event.getMessage().getContent().equals(Config.prefix + "shutdown") && event.getAuthor().getId().equals(Config.ownerId)){
            System.out.println("Shutting down!!!");
            timer.cancel();
            unbanTimer.cancel();
            event.getJDA().shutdown();
        }

        if(event.isFromType(ChannelType.PRIVATE) && !event.getJDA().getSelfUser().getId().equals(event.getAuthor().getId()) ){
            SkyBot.log(CustomLog.Level.WARNING, "User "+event.getMessage().getAuthor().getName()+"#"+event.getMessage().getAuthor().getDiscriminator()+", tried to do something in the pm-channel.\nThe message is " + event.getMessage().getContent());
            return;
        }
        if(event.isFromType(ChannelType.PRIVATE)){
            // NO JUST NO, RETURN THAT SHIT
            return;
        }
        if(event.getAuthor().isFake()){
            return;
        }
        if(event.getMember()==null) {
            return;
        }

        if(event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser()) && event.getTextChannel().canTalk()) {
            event.getChannel().sendMessage("Hey <@" + event.getAuthor().getId() + ">, try `" + Config.prefix + "help` for a list of commands. If it doesn't work scream at _duncte123#1245_").queue();
            return;
        }

        Permission[] adminPerms = {
                Permission.MESSAGE_MANAGE
        };
        if(PermissionUtil.checkPermission(event.getTextChannel(), event.getGuild().getSelfMember(), Permission.MESSAGE_MANAGE)) { //Bot has no perms :(
            if (!PermissionUtil.checkPermission(event.getMember(), adminPerms)) {
                Message messageToCheck = event.getMessage();
                if (filter.filterText(messageToCheck.getContent())) {
                    messageToCheck.delete().reason("Blocked for bad swearing: " + messageToCheck.getContent()).queue();
                    event.getChannel().sendMessage("Hello there, " + event.getAuthor().getAsMention() + " please do not use cursive language within this Discord.").queue(
                            m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                    return;
                }
            }
        }

        if(event.getMessage().getContent().startsWith(Config.prefix) && event.getMessage().getAuthor().getId() != event.getJDA().getSelfUser().getId()){
            // run the a command
            lastGuildChannel.put(event.getGuild(), event.getTextChannel());
            SkyBot.handleCommand(parser.parse(event.getMessage().getContent(), event));
            return;
        }

    }

    // when the bot is ready
    @Override
    public void onReady(ReadyEvent event){
        SkyBot.log(CustomLog.Level.INFO, "Logged in as " + event.getJDA().getSelfUser().getName());
        //event.getJDA().getGuilds().get(0).getPublicChannel().sendMessage(Main.defaultName+" V" + Config.version +" has been restarted.").queue();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {
                SkyBot.updateStatus();
            }
        };
        timer.schedule(myTask, 60*1000, 60*1000);

        /*TimerTask unbanTask = new TimerTask() {
            @Override
            public void run() {
                AirUtils.checkUnbans();
            }
        };*/
        //unbanTimer.schedule(unbanTask, DateUtils.MILLIS_PER_MINUTE, DateUtils.MILLIS_PER_MINUTE);

    }

    // when a new member joins the guild
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){

        if (AirUtils.blackList.contains(event.getGuild().getId())) return;

        TextChannel t = event.getGuild().getPublicChannel();
        String msg = "Welcome " + event.getMember().getAsMention() + ", to the official " + event.getGuild().getName() + " guild.";
        t.sendMessage(msg).queue();
    }

    //We will check if the bot is allowed to be in this guild
    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        if (AirUtils.whiteList.contains(event.getGuild().getId())) return;

        event.getGuild().getPublicChannel().sendMessage("Hey " + event.getGuild().getOwner().getAsMention()
                + ", I'm not made to be in this guild and will leave it in 20 seconds")
                .queue(
                    (m) -> event.getGuild().leave().queueAfter(20, TimeUnit.SECONDS)
        );
    }

    // leave channel and stop audio player when the channel is empty to prevent high data usage
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event){
        if(!event.getVoiceState().getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId()) && event.getGuild().getAudioManager().isConnected()){
            if (!event.getChannelLeft().getId().equals(event.getGuild().getAudioManager().getConnectedChannel().getId())) { return; }
            if(event.getChannelLeft().getMembers().size() <= 1){
                SkyBot.au.getMusicManager(event.getGuild()).player.stopTrack();
                SkyBot.au.getMusicManager(event.getGuild()).player.setPaused(false);
                SkyBot.au.getMusicManager(event.getGuild()).scheduler.queue.clear();
                lastGuildChannel.get(event.getGuild()).sendMessage(AirUtils.embedMessage("Leaving voice channel because all the members have left it.")).queue();
                if(event.getGuild().getAudioManager().isConnected()){
                    event.getGuild().getAudioManager().closeAudioConnection();
                    event.getGuild().getAudioManager().setSendingHandler(null);
                }
            }
        }
    }

}
