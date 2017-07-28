package me.duncte123.skybot;

import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.CustomLog;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class BotListener extends ListenerAdapter {

    // listen for messages
    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        if(event.getMessage().getContent().equals("/shutdown") && event.getAuthor().getId().equals("191231307290771456")){
            SkyBot.timer.cancel();
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

        Permission[] adminPerms = {
                Permission.MESSAGE_MANAGE
        };

        if (!PermissionUtil.checkPermission(event.getMember(), adminPerms)) {
            Message messageToCheck = event.getMessage();
            for (String badWord : Config.bannedWordList) {
                if (messageToCheck.getContent().toLowerCase().equals(badWord)) {
                    messageToCheck.delete().reason("Blocked for bad word: " + badWord).queue();
                    event.getChannel().sendMessage("Hello there, "+ event.getAuthor().getAsMention() + " please do not use cursive language within this Discord.").queue(
                            m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
              SkyBot.log(Config.defaultName+"Message", CustomLog.Level.INFO, "Message from user "+event.getMessage().getAuthor().getName()+"#"+event.getMessage().getAuthor().getDiscriminator()+": "+ event.getMessage().getContent());
                    return;
                }
            }
        }

        if(event.getMessage().getContent().startsWith(Config.prefix) && event.getMessage().getAuthor().getId() != event.getJDA().getSelfUser().getId()){
            // run the a command
            SkyBot.lastGuildChannel.put(event.getGuild(), event.getTextChannel());
            SkyBot.handleCommand(SkyBot.parser.parse(event.getMessage().getContent(), event));
            SkyBot.log(Config.defaultName+"Command", CustomLog.Level.INFO, "User "+event.getMessage().getAuthor().getName()+"#"+event.getMessage().getAuthor().getDiscriminator()+" ran command "+ event.getMessage().getContent().toLowerCase().split(" ")[0]);
            return;
        }
    
        SkyBot.log(Config.defaultName+"Message", CustomLog.Level.INFO, "Message from user "+event.getMessage().getAuthor().getName()+"#"+event.getMessage().getAuthor().getDiscriminator()+": "+ event.getMessage().getContent());
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

        SkyBot.timer.schedule(myTask, 60*1000, 60*1000);

    }

    // when a new member joins the guild
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){

        List<String> blackList = Arrays.asList("324453756794175488", "125227483518861312");

        if (blackList.contains(event.getGuild().getId())) return;

        TextChannel t = event.getGuild().getPublicChannel();
        String msg = "Welcome " + event.getMember().getAsMention() + ", to the official " + event.getGuild().getName() + " guild.";
        t.sendMessage(msg).queue();
    }

    //We will check if the bot is allowed to be in this guild
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        List<String> allowedGuilds = Arrays.asList(
                "329962158471512075",
                "125227483518861312",
                "324453756794175488",
                "292707924239712258",
                "191245668617158656"
        );

        if (allowedGuilds.contains(event.getGuild().getId())) return;

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
                SkyBot.lastGuildChannel.get(event.getGuild()).sendMessage(Functions.embedMessage("Leaving voice channel because all the members have left it.")).queue();
                if(event.getGuild().getAudioManager().isConnected()){
                    event.getGuild().getAudioManager().closeAudioConnection();
                    event.getGuild().getAudioManager().setSendingHandler(null);
                }
            }
        }
    }

}
