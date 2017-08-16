package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.audio.TrackScheduler;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.Config;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class RepeatCommand extends Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        if(!event.getGuild().getAudioManager().isConnected()){
            event.getChannel().sendMessage(AirUtils.embedField(SkyBot.au.embedTitle, "I'm not in a voice channel, use `"+Config.prefix+"join` to make me join a channel")).queue();
            return false;
        }

        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            event.getChannel().sendMessage(AirUtils.embedField(SkyBot.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        TrackScheduler scheduler = mng.scheduler;

        scheduler.setRepeating(!scheduler.isRepeating());

        event.getTextChannel().sendMessage(AirUtils.embedField(au.embedTitle, "Player was set to: **" + (scheduler.isRepeating() ? "repeat" : "not repeat") + "**")).queue();

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "Makes the player repeat the currently playing song";
    }

}
