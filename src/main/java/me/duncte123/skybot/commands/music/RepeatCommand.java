package me.duncte123.skybot.commands.music;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.audio.TrackScheduler;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class RepeatCommand implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        boolean inChan = false;
        boolean userInChan = true;
        EmbedBuilder eb = Functions.defaultEmbed();

        if(event.getGuild().getAudioManager().isConnected()){
            inChan = true;
        }else{
            eb.addField(SkyBot.au.embedTitle, "I'm not in a voice channel, use `"+Config.prefix+"join` to make me join a channel", false);
        }

        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            eb.addField(SkyBot.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands", false);
            userInChan = false;
        }

        if(!(inChan && userInChan)){
            event.getTextChannel().sendMessage(eb.build()).queue();
        }

        return inChan && userInChan;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        TrackScheduler scheduler = mng.scheduler;

        scheduler.setRepeating(!scheduler.isRepeating());

        event.getTextChannel().sendMessage(Functions.embedField(au.embedTitle, "Player was set to: **" + (scheduler.isRepeating() ? "repeat" : "not repeat") + "**")).queue();

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "Makes the player repeat the currently playing song";
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub

    }

}
