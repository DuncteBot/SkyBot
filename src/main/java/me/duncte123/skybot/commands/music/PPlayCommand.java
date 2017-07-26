package me.duncte123.skybot.commands.music;

import java.time.Instant;
import java.util.Arrays;

import me.duncte123.skybot.utils.Functions;
import org.apache.commons.lang3.StringUtils;
import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PPlayCommand implements Command {

    public final static String help = "add a playlist to the queue.";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        boolean inChan = false;
        boolean enoughArgs = false;
        EmbedBuilder eb = Functions.defaultEmbed();

        if(event.getGuild().getAudioManager().isConnected()){
            inChan = true;
        }else{
            eb.addField(SkyBot.au.embedTitle, "I'm not in a voice channel, use `"+Config.prefix+"join` to make me join a channel", false);
        }

        if(args.length > 0){
            enoughArgs = true;
        }else{
            eb.addField(SkyBot.au.embedTitle, "To few arguments, use `"+Config.prefix+"pplay <media link>`", false);
        }

        if(!(inChan && enoughArgs)){
            event.getTextChannel().sendMessage(eb.build()).queue();
        }

        return inChan && enoughArgs;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);

        String toPlay = StringUtils.join(Arrays.asList(args), " ");
        if(!SkyBot.isURL(toPlay)){
            toPlay = "ytsearch: " + toPlay;
        }

        au.loadAndPlay(mng, event.getTextChannel(), toPlay, true);

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub

    }

}
