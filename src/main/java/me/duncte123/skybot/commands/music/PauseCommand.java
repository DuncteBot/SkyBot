package me.duncte123.skybot.commands.music;

import java.time.Instant;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PauseCommand implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {


        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(Config.defaultColour)
            .addField(SkyBot.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands", false);
            eb.setFooter(Config.defaultName, Config.defaultIcon)
            .setTimestamp(Instant.now());
            event.getTextChannel().sendMessage(eb.build()).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        AudioPlayer player = mng.player;

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Config.defaultColour);

        if (player.getPlayingTrack() == null){
            eb.addField(au.embedTitle, "Cannot pause or resume player because no track is loaded for playing.", false)
            .setFooter(Config.defaultName, Config.defaultIcon)
            .setTimestamp(Instant.now());
            event.getTextChannel().sendMessage(eb.build()).queue();
            return;
        }

        player.setPaused(!player.isPaused());
        if (player.isPaused()){
            eb.addField(au.embedTitle, "The player has been paused.", false);
        }else{
            eb.addField(au.embedTitle, "The player has resumed playing.", false);
        }
        eb.setFooter(Config.defaultName, Config.defaultIcon)
        .setTimestamp(Instant.now());
        event.getTextChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "pauses the current song";
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub

    }

}
